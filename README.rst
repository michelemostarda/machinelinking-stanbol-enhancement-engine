--------------------------------------------------------------
MachineLinking API based Enhancement Engine for Apache Stanbol
--------------------------------------------------------------

Description
===========

This code provides a set of Enhancement Engines[1][2] for Apache Stanbol[3], based on
the MachineLinking[4] API[5].

This code is currently maintained by Michele Mostarda (**michele@machinelinking.com**).

Usage
=====

The code is a standard Maven3 module.

Before running any test you need to obtain a MachineLinking APP ID and KEY from 3scale
(https://machinelinking.3scale.net/login) 

Then you can run tests specifying APP ID and KEY as System properties:

    mvn -Dml.appid={app.id} -Dml.appkey={app.key} clean test

    mvn -Dml.appid={app.id} -Dml.appkey={app.key} install
    
You will also need to configure the Stanbol engines with the same `ml.appid` and `ml.appkey` properties.

----

[1] https://stanbol.apache.org/docs/trunk/components/enhancer/engines/list.html

[2] https://stanbol.apache.org/docs/trunk/components/enhancer/engines/index.html

[3] http://stanbol.apache.org/

[4] http://www.machinelinking.com/

[5] http://www.machinelinking.com/wp/documentation/
