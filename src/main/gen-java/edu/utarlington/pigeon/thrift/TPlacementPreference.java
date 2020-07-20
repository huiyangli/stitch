/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package edu.utarlington.pigeon.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2020-06-25")
public class TPlacementPreference implements org.apache.thrift.TBase<TPlacementPreference, TPlacementPreference._Fields>, java.io.Serializable, Cloneable, Comparable<TPlacementPreference> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TPlacementPreference");

  private static final org.apache.thrift.protocol.TField NODES_FIELD_DESC = new org.apache.thrift.protocol.TField("nodes", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField RACKS_FIELD_DESC = new org.apache.thrift.protocol.TField("racks", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField DELAY_THRESHOLD_FIELD_DESC = new org.apache.thrift.protocol.TField("delayThreshold", org.apache.thrift.protocol.TType.I32, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TPlacementPreferenceStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TPlacementPreferenceTupleSchemeFactory());
  }

  public List<String> nodes; // required
  public List<String> racks; // required
  public int delayThreshold; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NODES((short)1, "nodes"),
    RACKS((short)2, "racks"),
    DELAY_THRESHOLD((short)3, "delayThreshold");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // NODES
          return NODES;
        case 2: // RACKS
          return RACKS;
        case 3: // DELAY_THRESHOLD
          return DELAY_THRESHOLD;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __DELAYTHRESHOLD_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NODES, new org.apache.thrift.meta_data.FieldMetaData("nodes", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.RACKS, new org.apache.thrift.meta_data.FieldMetaData("racks", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.DELAY_THRESHOLD, new org.apache.thrift.meta_data.FieldMetaData("delayThreshold", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TPlacementPreference.class, metaDataMap);
  }

  public TPlacementPreference() {
  }

  public TPlacementPreference(
    List<String> nodes,
    List<String> racks,
    int delayThreshold)
  {
    this();
    this.nodes = nodes;
    this.racks = racks;
    this.delayThreshold = delayThreshold;
    setDelayThresholdIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TPlacementPreference(TPlacementPreference other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetNodes()) {
      List<String> __this__nodes = new ArrayList<String>(other.nodes);
      this.nodes = __this__nodes;
    }
    if (other.isSetRacks()) {
      List<String> __this__racks = new ArrayList<String>(other.racks);
      this.racks = __this__racks;
    }
    this.delayThreshold = other.delayThreshold;
  }

  public TPlacementPreference deepCopy() {
    return new TPlacementPreference(this);
  }

  @Override
  public void clear() {
    this.nodes = null;
    this.racks = null;
    setDelayThresholdIsSet(false);
    this.delayThreshold = 0;
  }

  public int getNodesSize() {
    return (this.nodes == null) ? 0 : this.nodes.size();
  }

  public java.util.Iterator<String> getNodesIterator() {
    return (this.nodes == null) ? null : this.nodes.iterator();
  }

  public void addToNodes(String elem) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<String>();
    }
    this.nodes.add(elem);
  }

  public List<String> getNodes() {
    return this.nodes;
  }

  public TPlacementPreference setNodes(List<String> nodes) {
    this.nodes = nodes;
    return this;
  }

  public void unsetNodes() {
    this.nodes = null;
  }

  /** Returns true if field nodes is set (has been assigned a value) and false otherwise */
  public boolean isSetNodes() {
    return this.nodes != null;
  }

  public void setNodesIsSet(boolean value) {
    if (!value) {
      this.nodes = null;
    }
  }

  public int getRacksSize() {
    return (this.racks == null) ? 0 : this.racks.size();
  }

  public java.util.Iterator<String> getRacksIterator() {
    return (this.racks == null) ? null : this.racks.iterator();
  }

  public void addToRacks(String elem) {
    if (this.racks == null) {
      this.racks = new ArrayList<String>();
    }
    this.racks.add(elem);
  }

  public List<String> getRacks() {
    return this.racks;
  }

  public TPlacementPreference setRacks(List<String> racks) {
    this.racks = racks;
    return this;
  }

  public void unsetRacks() {
    this.racks = null;
  }

  /** Returns true if field racks is set (has been assigned a value) and false otherwise */
  public boolean isSetRacks() {
    return this.racks != null;
  }

  public void setRacksIsSet(boolean value) {
    if (!value) {
      this.racks = null;
    }
  }

  public int getDelayThreshold() {
    return this.delayThreshold;
  }

  public TPlacementPreference setDelayThreshold(int delayThreshold) {
    this.delayThreshold = delayThreshold;
    setDelayThresholdIsSet(true);
    return this;
  }

  public void unsetDelayThreshold() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __DELAYTHRESHOLD_ISSET_ID);
  }

  /** Returns true if field delayThreshold is set (has been assigned a value) and false otherwise */
  public boolean isSetDelayThreshold() {
    return EncodingUtils.testBit(__isset_bitfield, __DELAYTHRESHOLD_ISSET_ID);
  }

  public void setDelayThresholdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __DELAYTHRESHOLD_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case NODES:
      if (value == null) {
        unsetNodes();
      } else {
        setNodes((List<String>)value);
      }
      break;

    case RACKS:
      if (value == null) {
        unsetRacks();
      } else {
        setRacks((List<String>)value);
      }
      break;

    case DELAY_THRESHOLD:
      if (value == null) {
        unsetDelayThreshold();
      } else {
        setDelayThreshold((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case NODES:
      return getNodes();

    case RACKS:
      return getRacks();

    case DELAY_THRESHOLD:
      return getDelayThreshold();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case NODES:
      return isSetNodes();
    case RACKS:
      return isSetRacks();
    case DELAY_THRESHOLD:
      return isSetDelayThreshold();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TPlacementPreference)
      return this.equals((TPlacementPreference)that);
    return false;
  }

  public boolean equals(TPlacementPreference that) {
    if (that == null)
      return false;

    boolean this_present_nodes = true && this.isSetNodes();
    boolean that_present_nodes = true && that.isSetNodes();
    if (this_present_nodes || that_present_nodes) {
      if (!(this_present_nodes && that_present_nodes))
        return false;
      if (!this.nodes.equals(that.nodes))
        return false;
    }

    boolean this_present_racks = true && this.isSetRacks();
    boolean that_present_racks = true && that.isSetRacks();
    if (this_present_racks || that_present_racks) {
      if (!(this_present_racks && that_present_racks))
        return false;
      if (!this.racks.equals(that.racks))
        return false;
    }

    boolean this_present_delayThreshold = true;
    boolean that_present_delayThreshold = true;
    if (this_present_delayThreshold || that_present_delayThreshold) {
      if (!(this_present_delayThreshold && that_present_delayThreshold))
        return false;
      if (this.delayThreshold != that.delayThreshold)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_nodes = true && (isSetNodes());
    list.add(present_nodes);
    if (present_nodes)
      list.add(nodes);

    boolean present_racks = true && (isSetRacks());
    list.add(present_racks);
    if (present_racks)
      list.add(racks);

    boolean present_delayThreshold = true;
    list.add(present_delayThreshold);
    if (present_delayThreshold)
      list.add(delayThreshold);

    return list.hashCode();
  }

  @Override
  public int compareTo(TPlacementPreference other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetNodes()).compareTo(other.isSetNodes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNodes()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.nodes, other.nodes);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetRacks()).compareTo(other.isSetRacks());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRacks()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.racks, other.racks);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDelayThreshold()).compareTo(other.isSetDelayThreshold());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDelayThreshold()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.delayThreshold, other.delayThreshold);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TPlacementPreference(");
    boolean first = true;

    sb.append("nodes:");
    if (this.nodes == null) {
      sb.append("null");
    } else {
      sb.append(this.nodes);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("racks:");
    if (this.racks == null) {
      sb.append("null");
    } else {
      sb.append(this.racks);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("delayThreshold:");
    sb.append(this.delayThreshold);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TPlacementPreferenceStandardSchemeFactory implements SchemeFactory {
    public TPlacementPreferenceStandardScheme getScheme() {
      return new TPlacementPreferenceStandardScheme();
    }
  }

  private static class TPlacementPreferenceStandardScheme extends StandardScheme<TPlacementPreference> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TPlacementPreference struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // NODES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.nodes = new ArrayList<String>(_list0.size);
                String _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = iprot.readString();
                  struct.nodes.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setNodesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // RACKS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list3 = iprot.readListBegin();
                struct.racks = new ArrayList<String>(_list3.size);
                String _elem4;
                for (int _i5 = 0; _i5 < _list3.size; ++_i5)
                {
                  _elem4 = iprot.readString();
                  struct.racks.add(_elem4);
                }
                iprot.readListEnd();
              }
              struct.setRacksIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // DELAY_THRESHOLD
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.delayThreshold = iprot.readI32();
              struct.setDelayThresholdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TPlacementPreference struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.nodes != null) {
        oprot.writeFieldBegin(NODES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.nodes.size()));
          for (String _iter6 : struct.nodes)
          {
            oprot.writeString(_iter6);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.racks != null) {
        oprot.writeFieldBegin(RACKS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.racks.size()));
          for (String _iter7 : struct.racks)
          {
            oprot.writeString(_iter7);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(DELAY_THRESHOLD_FIELD_DESC);
      oprot.writeI32(struct.delayThreshold);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TPlacementPreferenceTupleSchemeFactory implements SchemeFactory {
    public TPlacementPreferenceTupleScheme getScheme() {
      return new TPlacementPreferenceTupleScheme();
    }
  }

  private static class TPlacementPreferenceTupleScheme extends TupleScheme<TPlacementPreference> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TPlacementPreference struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetNodes()) {
        optionals.set(0);
      }
      if (struct.isSetRacks()) {
        optionals.set(1);
      }
      if (struct.isSetDelayThreshold()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetNodes()) {
        {
          oprot.writeI32(struct.nodes.size());
          for (String _iter8 : struct.nodes)
          {
            oprot.writeString(_iter8);
          }
        }
      }
      if (struct.isSetRacks()) {
        {
          oprot.writeI32(struct.racks.size());
          for (String _iter9 : struct.racks)
          {
            oprot.writeString(_iter9);
          }
        }
      }
      if (struct.isSetDelayThreshold()) {
        oprot.writeI32(struct.delayThreshold);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TPlacementPreference struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list10 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.nodes = new ArrayList<String>(_list10.size);
          String _elem11;
          for (int _i12 = 0; _i12 < _list10.size; ++_i12)
          {
            _elem11 = iprot.readString();
            struct.nodes.add(_elem11);
          }
        }
        struct.setNodesIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.racks = new ArrayList<String>(_list13.size);
          String _elem14;
          for (int _i15 = 0; _i15 < _list13.size; ++_i15)
          {
            _elem14 = iprot.readString();
            struct.racks.add(_elem14);
          }
        }
        struct.setRacksIsSet(true);
      }
      if (incoming.get(2)) {
        struct.delayThreshold = iprot.readI32();
        struct.setDelayThresholdIsSet(true);
      }
    }
  }

}

