# secret-service
![FlatpakUpdateAndRestart](top-secret.png)

[![Java CI with Gradle](https://github.com/purejava/secret-service/actions/workflows/build_main.yml/badge.svg)](https://github.com/purejava/FlatpakUpdateAndRestart/actions/workflows/build_main.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.purejava/secret-service.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=secret-service&smo=true&namespace=org.purejava)
[![License](https://img.shields.io/github/license/purejava/secret-service.svg)](https://github.com/purejava/secret-service/blob/main/LICENSE)

A Java library for managing secrets on Linux using the secret service DBus. It’s compatible to the GNOME keyring daemon and the kwallet daemon. The latter implements the Secret Service API since version v5.97.0, which was released in August 2022.

# Specification
The library implements the [Secret Service API Draft 0.2](https://specifications.freedesktop.org/secret-service-spec/latest/), paublished on 31st of August 2025.

# Dependency
Add `secret-service` as a dependency to your project.

## Gradle
```gradle
implementation group: 'org.purejava', name: 'secret-service', version: '1.1.0'
```
## Maven

```maven
<dependency>
   <groupId>org.purejava</groupId>
   <artifactId>secret-service</artifactId>
   <version>1.1.0</version>
</dependency>
```

# Documentation
For documentation please take a look at the [Wiki](https://github.com/purejava/secret-service/wiki).

# Donation
It took me about a month to develop and test this library. You can use it for free. If you like this project, why not buy me a cup of coffee?

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/donate?hosted_button_id=XVX9ZM7WE4ANL)

# Copyright
Copyright (C) 2025 Ralph Plawetzki