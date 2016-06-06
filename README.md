# IBASHO SPID Discovery

La soluzione Open Source che ha preso vita implementa le nuove specifiche **SPID** per la Pubblica Amministrazione
e lo fa utilizzando configurazioni e tecnologie già consolidate ([Shibboleth][shibboleth])
e di semplice introduzione/applicazione.

È possibile scaricare **IBASHO** (questo il nome del progetto open source creato e disponibile all’indirizzo
[http://open.publisys.it][openpublisys]) e utilizzarlo come **Service Provider** per lo **SPID**
e/o come **Identity Provider** interno per la organizzazione dell’amministrazione.

**IBASHO** è il primo prodotto Open Source che facilita l'adozione di SPID in un’organizzazione pubblica.
Le componenti rese disponibili sono le seguenti:

* **Identity Provider**: sistema di gestione dei servizi interni all’organizzazione con la possibilità di configurare tramite XML l’interazione con lo SPID

* **Discovery Service**: servizio di integrazione delle soluzioni di accreditamento già presenti nella tua organizzazione con i nuovi provider (IdPs) accreditati presso AGID. Potrai consentire ai tuoi utenti di poter accedere ai servizi della tua amministrazione sia con il tuo “vecchio” sistema (garantendone l’accesso per i 2 anni previsti dall’attivazione di SPID) sia con i nuovi Identity Provider accreditati tramite SPID

* **Liferay Hook**: hook sviluppato per poter integrare un portale web Liferay con il sistema di Discovery Service e rendere trasparente l’integrazione per un portale già esistente


### Generazione WAR
Nel POM (Project Object Model) Maven, creare un profile (come il blocco sottostante) modificando i valori delle properties
con i dati della propria installazione e creare un file **idp.xml** contenente i dati degli IdP non SPID.

###### idp.xml (sample)

```sh
<?xml version="1.0" encoding="UTF-8"?>
<idps>
    <idp alias="idp-1">
        <entity-id>idp-1-eid</entity-id>
        <name>IdP 1</name>
        <description>
            description 1
        </description>
        <signup-url>https://www.idp.it/registrazione</signup-url>
        <header-image>/css/img/spid/publisys140.png</header-image>
        <label-image>/css/img/spid/publisys.png</label-image>
        <button-css-class>button-custom</button-css-class>
    </idp>
    <idp alias="idp-2">
        <entity-id>idp-2-eid</entity-id>
        <name>IdP 2</name>
        <description>
            description 2
        </description>
        <signup-url></signup-url>
        <header-image>/css/img/spid/publisys140.png</header-image>
        <label-image>/css/img/spid/publisys.png</label-image>
        <button-css-class>button-custom</button-css-class>
    </idp>
</idps>
```

###### pom.xml (profile template)

```sh
...
<profile>
    <id>dev</id>
    <properties>
        <spring.jpa.database>H2</spring.jpa.database>
        <spring.datasource.driver-class-name>org.h2.Driver</spring.datasource.driver-class-name>
        <spring.datasource.url>jdbc:h2:mem:spiddb</spring.datasource.url>
        <spring.datasource.username>sa</spring.datasource.username>
        <spring.datasource.password></spring.datasource.password>
        <!-- KEYSTORE -->
        <idp.sec.keystore.file>PATH_CLASSES_DIR/metadata/idp/keystore/guanxi_idp.jks</idp.sec.keystore.file>
        <idp.sec.keystore.pass>KEYSTORE_PASSWORD</idp.sec.keystore.pass>
        <idp.sec.keystore.type>JKS</idp.sec.keystore.type>
        <idp.sec.private.key.alias>KEYSTORE_ALIAS</idp.sec.private.key.alias>
        <idp.sec.private.key.pass>PRIVATEKEY_PASSWORD</idp.sec.private.key.pass>
        <idp.sec.certificate.alias>PRIVATEKEY_ALIAS</idp.sec.certificate.alias>
        <!-- SHIBBOLETH -->
        <spid.domain>DOMAIN_SPID</spid.domain>
        <idp.discovery.issuer>https://${spid.domain}/idp-discovery</idp.discovery.issuer>
        <spid.backurl>https://${spid.domain}/ims-discovery/sp</spid.backurl>
        <spid.shib.logout>https://${spid.domain}/Shibboleth.sso/Logout</spid.shib.logout>
        <spid.shib.sso.login>https://${spid.domain}/Shibboleth.sso/Login</spid.shib.sso.login>
        <spid.entityid.infocert>ENTITYID_INFOCERT</spid.entityid.infocert>
        <spid.entityid.poste>ENTITYID_POSTE</spid.entityid.poste>
        <spid.entityid.telecom>ENTITYID_TELECOM</spid.entityid.telecom>
        <spid.idp.file>idp.xml</spid.idp.file>
        <!-- I18N -->
        <i18n.basename>i18n/dev_messages</i18n.basename>
    </properties>
    <dependencies>
        <!-- dipendenza per escludere il tomcat embedded utilizzato con Spring Boot e permettere la generazione di un WAR deployabile sotto un application container (Tomcat ad esempio) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</profile>
...
```

Effettuata la precedente configurazione è possibile eseguire il comando
```sh
$ mvn clean package -Pdev
```
dove **dev** è l'**ID** del profilo creato nel pom.xml.

Eseguito il comando è possibile installare il WAR generato dalla directory target sull'application container desiderato.

> Per la generazione dei certificati è presente nella classe di test **ImsDiscoveryApplicationTests** il metodo **test000GenerateCertificate**.

### Prerequisiti

- Installazione di uno **Shibboleth Service Provider** opportunamente configurato per gestire l'**AuthnRequest** come da **regole tecniche AGID**


[Publisys S.p.A.][publisys]

[publisys]: <http://www.publisys.it>
[openpublisys]: <http://open.publisys.it>
[shibboleth]: <https://shibboleth.net>
