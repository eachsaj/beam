/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.coders;

import org.apache.beam.sdk.util.CloudObject;
import org.apache.beam.sdk.util.Structs;
import org.apache.beam.sdk.util.VarInt;

import com.google.common.io.ByteStreams;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * A coder for JAXB annotated objects. This coder uses JAXB marshalling/unmarshalling mechanisms
 * to encode/decode the objects. Users must provide the {@code Class} of the JAXB annotated object.
 *
 * @param <T> type of JAXB annotated objects that will be serialized.
 */
public class JAXBCoder<T> extends AtomicCoder<T> {

  private final Class<T> jaxbClass;
  private transient volatile JAXBContext jaxbContext;

  public Class<T> getJAXBClass() {
    return jaxbClass;
  }

  private JAXBCoder(Class<T> jaxbClass) {
    this.jaxbClass = jaxbClass;
  }

  /**
   * Create a coder for a given type of JAXB annotated objects.
   *
   * @param jaxbClass the {@code Class} of the JAXB annotated objects.
   */
  public static <T> JAXBCoder<T> of(Class<T> jaxbClass) {
    return new JAXBCoder<>(jaxbClass);
  }

  @Override
  public void encode(T value, OutputStream outStream, Context context)
      throws CoderException, IOException {
    try {
      JAXBContext jaxbContext = getContext();
      // TODO: Consider caching in a ThreadLocal if this impacts performance
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      if (!context.isWholeStream) {
        try {
          long size = getEncodedElementByteSize(value, Context.OUTER);
          // record the number of bytes the XML consists of so when reading we only read the encoded
          // value
          VarInt.encode(size, outStream);
        } catch (Exception e) {
          throw new CoderException(
              "An Exception occured while trying to get the size of an encoded representation", e);
        }
      }

      jaxbMarshaller.marshal(value, new CloseIgnoringOutputStream(outStream));
    } catch (JAXBException e) {
      throw new CoderException(e);
    }
  }

  @Override
  public T decode(InputStream inStream, Context context) throws CoderException, IOException {
    try {
      JAXBContext jaxbContext = getContext();
      // TODO: Consider caching in a ThreadLocal if this impacts performance
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      InputStream stream = inStream;
      if (!context.isWholeStream) {
        long limit = VarInt.decodeLong(inStream);
        stream = ByteStreams.limit(inStream, limit);
      }
      @SuppressWarnings("unchecked")
      T obj = (T) jaxbUnmarshaller.unmarshal(new CloseIgnoringInputStream(stream));
      return obj;
    } catch (JAXBException e) {
      throw new CoderException(e);
    }
  }

  private JAXBContext getContext() throws JAXBException {
    if (jaxbContext == null) {
      synchronized (this) {
        if (jaxbContext == null) {
          jaxbContext = JAXBContext.newInstance(jaxbClass);
        }
      }
    }
    return jaxbContext;
  }

  @Override
  public String getEncodingId() {
    return getJAXBClass().getName();
  }

  private static class CloseIgnoringInputStream extends FilterInputStream {

    protected CloseIgnoringInputStream(InputStream in) {
      super(in);
    }

    @Override
    public void close() {
      // Do nothing. JAXB closes the underlying stream so we must filter out those calls.
    }
  }

  private static class CloseIgnoringOutputStream extends FilterOutputStream {

    protected CloseIgnoringOutputStream(OutputStream out) {
      super(out);
    }

    @Override
    public void close() throws IOException {
      // JAXB closes the underlying stream so we must filter out those calls.
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // JSON Serialization details below

  private static final String JAXB_CLASS = "jaxb_class";

  /**
   * Constructor for JSON deserialization only.
   */
  @JsonCreator
  public static <T> JAXBCoder<T> of(
      @JsonProperty(JAXB_CLASS) String jaxbClassName) {
    try {
      @SuppressWarnings("unchecked")
      Class<T> jaxbClass = (Class<T>) Class.forName(jaxbClassName);
      return of(jaxbClass);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public CloudObject asCloudObject() {
    CloudObject result = super.asCloudObject();
    Structs.addString(result, JAXB_CLASS, jaxbClass.getName());
    return result;
  }
}
