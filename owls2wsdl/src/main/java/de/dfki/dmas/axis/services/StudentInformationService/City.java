/**
 * City.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.dfki.dmas.axis.services.StudentInformationService;

public class City  implements java.io.Serializable {
    private java.lang.String name;

    private de.dfki.dmas.axis.services.StudentInformationService.Country belongsTo;

    public City() {
    }

    public City(
           java.lang.String name,
           de.dfki.dmas.axis.services.StudentInformationService.Country belongsTo) {
           this.name = name;
           this.belongsTo = belongsTo;
    }


    /**
     * Gets the name value for this City.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this City.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the belongsTo value for this City.
     * 
     * @return belongsTo
     */
    public de.dfki.dmas.axis.services.StudentInformationService.Country getBelongsTo() {
        return belongsTo;
    }


    /**
     * Sets the belongsTo value for this City.
     * 
     * @param belongsTo
     */
    public void setBelongsTo(de.dfki.dmas.axis.services.StudentInformationService.Country belongsTo) {
        this.belongsTo = belongsTo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof City)) return false;
        City other = (City) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.belongsTo==null && other.getBelongsTo()==null) || 
             (this.belongsTo!=null &&
              this.belongsTo.equals(other.getBelongsTo())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getBelongsTo() != null) {
            _hashCode += getBelongsTo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(City.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "City"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("belongsTo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "belongsTo"));
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
