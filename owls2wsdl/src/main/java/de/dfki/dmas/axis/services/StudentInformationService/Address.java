/**
 * Address.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.dfki.dmas.axis.services.StudentInformationService;

public class Address  implements java.io.Serializable {
    private java.lang.String street;

    private java.lang.String includesZipCode;

    private de.dfki.dmas.axis.services.StudentInformationService.City includesCity;

    private de.dfki.dmas.axis.services.StudentInformationService.Country includesCountry;

    public Address() {
    }

    public Address(
           java.lang.String street,
           java.lang.String includesZipCode,
           de.dfki.dmas.axis.services.StudentInformationService.City includesCity,
           de.dfki.dmas.axis.services.StudentInformationService.Country includesCountry) {
           this.street = street;
           this.includesZipCode = includesZipCode;
           this.includesCity = includesCity;
           this.includesCountry = includesCountry;
    }


    /**
     * Gets the street value for this Address.
     * 
     * @return street
     */
    public java.lang.String getStreet() {
        return street;
    }


    /**
     * Sets the street value for this Address.
     * 
     * @param street
     */
    public void setStreet(java.lang.String street) {
        this.street = street;
    }


    /**
     * Gets the includesZipCode value for this Address.
     * 
     * @return includesZipCode
     */
    public java.lang.String getIncludesZipCode() {
        return includesZipCode;
    }


    /**
     * Sets the includesZipCode value for this Address.
     * 
     * @param includesZipCode
     */
    public void setIncludesZipCode(java.lang.String includesZipCode) {
        this.includesZipCode = includesZipCode;
    }


    /**
     * Gets the includesCity value for this Address.
     * 
     * @return includesCity
     */
    public de.dfki.dmas.axis.services.StudentInformationService.City getIncludesCity() {
        return includesCity;
    }


    /**
     * Sets the includesCity value for this Address.
     * 
     * @param includesCity
     */
    public void setIncludesCity(de.dfki.dmas.axis.services.StudentInformationService.City includesCity) {
        this.includesCity = includesCity;
    }


    /**
     * Gets the includesCountry value for this Address.
     * 
     * @return includesCountry
     */
    public de.dfki.dmas.axis.services.StudentInformationService.Country getIncludesCountry() {
        return includesCountry;
    }


    /**
     * Sets the includesCountry value for this Address.
     * 
     * @param includesCountry
     */
    public void setIncludesCountry(de.dfki.dmas.axis.services.StudentInformationService.Country includesCountry) {
        this.includesCountry = includesCountry;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Address)) return false;
        Address other = (Address) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.street==null && other.getStreet()==null) || 
             (this.street!=null &&
              this.street.equals(other.getStreet()))) &&
            ((this.includesZipCode==null && other.getIncludesZipCode()==null) || 
             (this.includesZipCode!=null &&
              this.includesZipCode.equals(other.getIncludesZipCode()))) &&
            ((this.includesCity==null && other.getIncludesCity()==null) || 
             (this.includesCity!=null &&
              this.includesCity.equals(other.getIncludesCity()))) &&
            ((this.includesCountry==null && other.getIncludesCountry()==null) || 
             (this.includesCountry!=null &&
              this.includesCountry.equals(other.getIncludesCountry())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getStreet() != null) {
            _hashCode += getStreet().hashCode();
        }
        if (getIncludesZipCode() != null) {
            _hashCode += getIncludesZipCode().hashCode();
        }
        if (getIncludesCity() != null) {
            _hashCode += getIncludesCity().hashCode();
        }
        if (getIncludesCountry() != null) {
            _hashCode += getIncludesCountry().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Address.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "Address"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("street");
        elemField.setXmlName(new javax.xml.namespace.QName("", "street"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includesZipCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "includesZipCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includesCity");
        elemField.setXmlName(new javax.xml.namespace.QName("", "includesCity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "City"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includesCountry");
        elemField.setXmlName(new javax.xml.namespace.QName("", "includesCountry"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "Country"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
