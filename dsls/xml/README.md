<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# Example Pipelines- DSL Declarative API

The examples included in this module serve to demonstrate the basic
functionality of Apache Beam, and act as starting points for
the development of more complex pipelines.

## Word Count

Few examples using Apache Beam Declarative API.

1. [`WordCount`]is the simplest word count pipeline and introduces basic beam DSL concepts like [Pipelines](https://beam.apache.org/documentation/programming-guide/#pipeline),
[PCollections](https://beam.apache.org/documentation/programming-guide/#pcollection),
[ParDo](https://beam.apache.org/documentation/programming-guide/#transforms-pardo),
and [reading and writing data](https://beam.apache.org/documentation/programming-guide/#io) from external storage.

## Running Examples
To run the XML based example applications run  the class
 org.apache.beam.dsls.xml.flow.loader.BeamFlowDSLRunner with an argument --dslXm=Your flow xml file
Try with example (dsls/xml/src/main/java/org/apache/beam/dsls/xml/example/wordcount/BeamFlow-File-in-File-out.xml)
#### How to run the example with DSL
java org.apache.beam.dsls.xml.flow.loader.BeamFlowDSLRunner --dslXml= "your flow xml  file absolute path".
	
