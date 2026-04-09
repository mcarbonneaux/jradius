package net.jradius.tls;

import org.bouncycastle.asn1.x509.Certificate;

/**
 * A certificate verifyer, that will always return true.
 * 
 * <pre>
 * DO NOT USE THIS FILE UNLESS YOU KNOW EXACTLY WHAT YOU ARE DOING.
 * </pre>
 */
public class AlwaysValidVerifyer implements CertificateVerifyer
{
    /**
     * Return true.
     * 
     * @see CertificateVerifyer#isValid(org.bouncycastle.asn1.x509.Certificate[])
     */
    public boolean isValid(Certificate[] certs)
    {
        return true;
    }
}
