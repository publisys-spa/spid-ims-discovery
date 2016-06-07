package it.publisys.ims.discovery;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = ImsDiscoveryApplication.class)
//@WebAppConfiguration
public class ImsDiscoveryApplicationTests {

    @Test
    public void test000GenerateCertificate() {
        String cn = "www.example.it";
        String keystoreFile = "guanxi_idp_cert.jks";
        String keystorePassword = "changeit";
        String privateKeyPassword = "changeit";
        String privateKeyAlias = "www.example.it";

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        KeyStore ks = null;

        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(null, null);

//            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024, new SecureRandom());
            KeyPair keypair = keyGen.generateKeyPair();
            PrivateKey privkey = keypair.getPrivate();
            PublicKey pubkey = keypair.getPublic();

            Hashtable<DERObjectIdentifier, String> attrs = new Hashtable<DERObjectIdentifier, String>();
            Vector<DERObjectIdentifier> ordering = new Vector<DERObjectIdentifier>();
            ordering.add(X509Name.CN);
            attrs.put(X509Name.CN, cn);
            X509Name issuerDN = new X509Name(ordering, attrs);
            X509Name subjectDN = new X509Name(ordering, attrs);

            Date validFrom = new Date();
            validFrom.setTime(validFrom.getTime() - (10 * 60 * 1000));

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 10);

            Date validTo = new Date();
            validTo.setTime(cal.getTime().getTime());
//            validTo.setTime(validTo.getTime() + (20 * (24 * 60 * 60 * 1000)));

            X509V3CertificateGenerator x509 = new X509V3CertificateGenerator();
            //x509.setSignatureAlgorithm("SHA1withDSA");
            x509.setSignatureAlgorithm("SHA256withRSA");
            x509.setIssuerDN(issuerDN);
            x509.setSubjectDN(subjectDN);
            x509.setPublicKey(pubkey);
            x509.setNotBefore(validFrom);
            x509.setNotAfter(validTo);
            x509.setSerialNumber(new BigInteger(128, new Random()));

            X509Certificate[] cert = new X509Certificate[1];
            cert[0] = x509.generate(privkey, "BC");
            java.security.cert.Certificate[] chain = new java.security.cert.Certificate[1];
            chain[0] = cert[0];

            ks.setKeyEntry(privateKeyAlias, privkey, privateKeyPassword.toCharArray(), cert);
            ks.setKeyEntry(privateKeyAlias, privkey, privateKeyPassword.toCharArray(), chain);
            ks.store(new FileOutputStream(keystoreFile), keystorePassword.toCharArray());

            String IDP_RFC_CERT = "/tmp/guanxi_idp_cert.txt";

            PEMWriter pemWriter = new PEMWriter(new FileWriter(IDP_RFC_CERT));
            pemWriter.writeObject(cert[0]);
            pemWriter.close();

        } catch (Exception se) {
            se.printStackTrace(System.err);
        }
    }

}
