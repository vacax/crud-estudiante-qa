package edu.pucmm.estudiante.aceptacion;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/** Punto de entrada de Gradle: ejecuta los archivos .feature con el motor de Cucumber. */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "edu.pucmm.estudiante.aceptacion")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value =
        "pretty,"
            + "html:build/reports/cucumber/cucumber.html,"
            + "json:build/reports/cucumber/cucumber.json,"
            + "junit:build/test-results/acceptanceTest/cucumber.xml")
public class SuiteAceptacion {}
