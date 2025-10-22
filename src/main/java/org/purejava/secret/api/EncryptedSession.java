package org.purejava.secret.api;

import at.favre.lib.hkdf.HKDF;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class EncryptedSession {
    public static final int PRIVATE_VALUE_BITS = 1024;
    public static final int AES_BITS = 128;
    private final Service service;
    private DBusPath session = null;
    private DHParameterSpec dhParameters = null;
    private KeyPair keypair = null;
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;
    private SecretKey sessionKey = null;
    private byte[] yb = null;

    public EncryptedSession() {
        this.service = new Service();
    }

    public EncryptedSession(Service service) {
        this.service = service;
    }

    static private BigInteger fromBinary(byte[] bytes) {
        return new BigInteger(1, bytes);
    }

    static private int toBytes(int bits) {
        return bits / 8;
    }

    public void initialize() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        // create dh parameter specification with prime, generator and bits
        BigInteger prime = fromBinary(RFC_7296.SecondOakleyGroup.PRIME);
        BigInteger generator = fromBinary(RFC_7296.SecondOakleyGroup.GENERATOR);
        dhParameters = new DHParameterSpec(prime, generator, PRIVATE_VALUE_BITS);

        // generate DH keys from specification
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(Algorithm.DIFFIE_HELLMAN);
        keyPairGenerator.initialize(dhParameters);
        keypair = keyPairGenerator.generateKeyPair();
        publicKey = keypair.getPublic();
        privateKey = keypair.getPrivate();
    }

    public boolean openSession() {
        if (keypair == null) {
            throw new IllegalStateException("Missing own keypair. Call initialize() first.");
        }

        // The public keys are transferred as an array of bytes representing an unsigned integer of arbitrary size,
        // most-significant byte first (e.g., the integer 32768 is represented as the 2-byte string 0x80 0x00)
        BigInteger ya = ((DHPublicKey) publicKey).getY();

        // open session with "Client DH pub key as an array of bytes" without prime or generator
        Pair<Variant<ArrayList<Byte>>, DBusPath> osResponse = service.openSession(
                Algorithm.DH_IETF1024_SHA256_AES128_CBC_PKCS7, new Variant<>(ya.toByteArray()));

        // transform peer's raw Y to a public key
        if (osResponse != null) {
            ArrayList<Byte> list = osResponse.a.getValue();
            yb = new byte[list.size()];
            IntStream.range(0, list.size()).forEach(i -> yb[i] = list.get(i));
            session = osResponse.b;
            return true;
        } else {
            return false;
        }
    }

    public void generateSessionKey() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        if (yb == null) {
            throw new IllegalStateException("Missing peer public key. Call openSession() first.");
        }

        DHPublicKeySpec dhPublicKeySpec = new DHPublicKeySpec(fromBinary(yb), dhParameters.getP(), dhParameters.getG());
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.DIFFIE_HELLMAN);
        DHPublicKey peerPublicKey = (DHPublicKey) keyFactory.generatePublic(dhPublicKeySpec);

        KeyAgreement keyAgreement = KeyAgreement.getInstance(Algorithm.DIFFIE_HELLMAN);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(peerPublicKey, true);
        byte[] rawSessionKey = keyAgreement.generateSecret();

        // HKDF digest into a 128-bit key by extract and expand with "NULL salt and empty info"
        // see: https://datatracker.ietf.org/doc/html/rfc7296#appendix-B.2
        byte[] pseudoRandomKey = HKDF.fromHmacSha256().extract((byte[]) null, rawSessionKey);
        byte[] keyingMaterial = HKDF.fromHmacSha256().expand(pseudoRandomKey, null, toBytes(AES_BITS));

        sessionKey = new SecretKeySpec(keyingMaterial, Algorithm.AES);
    }

    public Secret encrypt(CharSequence plain) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        final byte[] bytes = Secret.toBytes(plain);
        try {
            return encrypt(bytes, StandardCharsets.UTF_8);
        } finally {
            Secret.clear(bytes);
        }
    }

    public Secret encrypt(byte[] plain, Charset charset) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        if (plain == null) return null;

        if (service == null) {
            throw new IllegalStateException("Missing session. Call openSession() first.");
        }
        if (sessionKey == null) {
            throw new IllegalStateException("Missing session key. Call generateSessionKey() first.");
        }

        // secret.parameter - 16 byte AES initialization vector
        final byte[] salt = new byte[toBytes(AES_BITS)];
        SecureRandom random = SecureRandom.getInstance(Algorithm.SHA1_PRNG);
        random.nextBytes(salt);
        IvParameterSpec ivSpec = new IvParameterSpec(salt);

        Cipher cipher = Cipher.getInstance(Algorithm.AES_CBC_PKCS5);
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);

        String contentType = Secret.createContentType(charset);

        return new Secret(session, ivSpec.getIV(), cipher.doFinal(plain), contentType);
    }

    public char[] decrypt(Secret secret) throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException {

        if (secret == null) return null;

        if (sessionKey == null) {
            throw new IllegalStateException("Missing session key. Call generateSessionKey() first.");
        }

        IvParameterSpec ivSpec = new IvParameterSpec(secret.getSecretParameters());
        Cipher cipher = Cipher.getInstance(Algorithm.AES_CBC_PKCS5);
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);
        final byte[] decrypted = cipher.doFinal(secret.getSecretValue());
        try {
            return Secret.toChars(decrypted);
        } finally {
            Secret.clear(decrypted);
        }
    }

    public Service getService() {
        return service;
    }

    public DBusPath getSession() { return session; }

    public void clear() {
        if (privateKey != null) try {
            privateKey.destroy();
        } catch (DestroyFailedException e) {
            Secret.clear(privateKey.getEncoded());
        }
        if (sessionKey != null) try {
            sessionKey.destroy();
        } catch (DestroyFailedException e) {
            Secret.clear(sessionKey.getEncoded());
        }
    }

    public static class Algorithm {
        public static final String PLAIN = "plain";
        public static final String DH_IETF1024_SHA256_AES128_CBC_PKCS7 = "dh-ietf1024-sha256-aes128-cbc-pkcs7";
        public static final String DIFFIE_HELLMAN = "DH";
        public static final String AES = "AES";
        public static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
        public static final String SHA1_PRNG = "SHA1PRNG";
    }

    public static class RFC_7296 {

        /**
         * RFC 7296: https://datatracker.ietf.org/doc/html/rfc7296#appendix-B.2
         */
        public static class SecondOakleyGroup {

            public static final byte[] PRIME = new byte[]{
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xC9, (byte) 0x0F, (byte) 0xDA, (byte) 0xA2, (byte) 0x21, (byte) 0x68, (byte) 0xC2, (byte) 0x34,
                    (byte) 0xC4, (byte) 0xC6, (byte) 0x62, (byte) 0x8B, (byte) 0x80, (byte) 0xDC, (byte) 0x1C, (byte) 0xD1,
                    (byte) 0x29, (byte) 0x02, (byte) 0x4E, (byte) 0x08, (byte) 0x8A, (byte) 0x67, (byte) 0xCC, (byte) 0x74,
                    (byte) 0x02, (byte) 0x0B, (byte) 0xBE, (byte) 0xA6, (byte) 0x3B, (byte) 0x13, (byte) 0x9B, (byte) 0x22,
                    (byte) 0x51, (byte) 0x4A, (byte) 0x08, (byte) 0x79, (byte) 0x8E, (byte) 0x34, (byte) 0x04, (byte) 0xDD,
                    (byte) 0xEF, (byte) 0x95, (byte) 0x19, (byte) 0xB3, (byte) 0xCD, (byte) 0x3A, (byte) 0x43, (byte) 0x1B,
                    (byte) 0x30, (byte) 0x2B, (byte) 0x0A, (byte) 0x6D, (byte) 0xF2, (byte) 0x5F, (byte) 0x14, (byte) 0x37,
                    (byte) 0x4F, (byte) 0xE1, (byte) 0x35, (byte) 0x6D, (byte) 0x6D, (byte) 0x51, (byte) 0xC2, (byte) 0x45,
                    (byte) 0xE4, (byte) 0x85, (byte) 0xB5, (byte) 0x76, (byte) 0x62, (byte) 0x5E, (byte) 0x7E, (byte) 0xC6,
                    (byte) 0xF4, (byte) 0x4C, (byte) 0x42, (byte) 0xE9, (byte) 0xA6, (byte) 0x37, (byte) 0xED, (byte) 0x6B,
                    (byte) 0x0B, (byte) 0xFF, (byte) 0x5C, (byte) 0xB6, (byte) 0xF4, (byte) 0x06, (byte) 0xB7, (byte) 0xED,
                    (byte) 0xEE, (byte) 0x38, (byte) 0x6B, (byte) 0xFB, (byte) 0x5A, (byte) 0x89, (byte) 0x9F, (byte) 0xA5,
                    (byte) 0xAE, (byte) 0x9F, (byte) 0x24, (byte) 0x11, (byte) 0x7C, (byte) 0x4B, (byte) 0x1F, (byte) 0xE6,
                    (byte) 0x49, (byte) 0x28, (byte) 0x66, (byte) 0x51, (byte) 0xEC, (byte) 0xE6, (byte) 0x53, (byte) 0x81,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            };

            public static final byte[] GENERATOR = new byte[]{(byte) 0x02};

        }
    }
}
