/**
 * Student.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.dfki.dmas.axis.services.StudentInformationService;

public class Student  implements java.io.Serializable {
    private java.lang.String name;

    private de.dfki.dmas.axis.services.StudentInformationService.Address hasAddress;

    private java.lang.String schoolyear;

    private de.dfki.dmas.axis.services.StudentInformationService.Course enrolledIn;

    public Student() {
    }

    public Student(
           java.lang.String name,
           de.dfki.dmas.axis.services.StudentInformationService.Address hasAddress,
           java.lang.String schoolyear,
           de.dfki.dmas.axis.services.StudentInformationService.Course enrolledIn) {
           this.name = name;
           this.hasAddress = hasAddress;
           this.schoolyear = schoolyear;
           this.enrolledIn = enrolledIn;
    }


    /**
     * Gets the name value for this Student.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Student.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the hasAddress value for this Student.
     * 
     * @return hasAddress
     */
    public de.dfki.dmas.axis.services.StudentInformationService.Address getHasAddress() {
        return hasAddress;
    }


    /**
     * Sets the hasAddress value for this Student.
     * 
     * @param hasAddress
     */
    public void setHasAddress(de.dfki.dmas.axis.services.StudentInformationService.Address hasAddress) {
        this.hasAddress = hasAddress;
    }


    /**
     * Gets the schoolyear value for this Student.
     * 
     * @return schoolyear
     */
    public java.lang.String getSchoolyear() {
        return schoolyear;
    }


    /**
     * Sets the schoolyear value for this Student.
     * 
     * @param schoolyear
     */
    public void setSchoolyear(java.lang.String schoolyear) {
        this.schoolyear = schoolyear;
    }


    /**
     * Gets the enrolledIn value for this Student.
     * 
     * @return enrolledIn
     */
    public de.dfki.dmas.axis.services.StudentInformationService.Course getEnrolledIn() {
        return enrolledIn;
    }


    /**
     * Sets the enrolledIn value for this Student.
     * 
     * @param enrolledIn
     */
    public void setEnrolledIn(de.dfki.dmas.axis.services.StudentInformationService.Course enrolledIn) {
        this.enrolledIn = enrolledIn;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Student)) return false;
        Student other = (Student) obj;
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
            ((this.hasAddress==null && other.getHasAddress()==null) || 
             (this.hasAddress!=null &&
              this.hasAddress.equals(other.getHasAddress()))) &&
            ((this.schoolyear==null && other.getSchoolyear()==null) || 
             (this.schoolyear!=null &&
              this.schoolyear.equals(other.getSchoolyear()))) &&
            ((this.enrolledIn==null && other.getEnrolledIn()==null) || 
             (this.enrolledIn!=null &&
              this.enrolledIn.equals(other.getEnrolledIn())));
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
        if (getHasAddress() != null) {
            _hashCode += getHasAddress().hashCode();
        }
        if (getSchoolyear() != null) {
            _hashCode += getSchoolyear().hashCode();
        }
        if (getEnrolledIn() != null) {
            _hashCode += getEnrolledIn().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Student.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "Student"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hasAddress");
        elemField.setXmlName(new javax.xml.namespace.QName("", "hasAddress"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "Address"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("schoolyear");
        elemField.setXmlName(new javax.xml.namespace.QName("", "schoolyear"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("enrolledIn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "enrolledIn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://dmas.dfki.de/axis/services/StudentInformationService", "Course"));
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
