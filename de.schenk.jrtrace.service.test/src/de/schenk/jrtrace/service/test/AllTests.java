/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.schenk.jrtrace.enginex.reinstall.SimpleReinstallTest;
import de.schenk.jrtrace.enginex.testscripts.EngineXAnnotationReaderTest;
import de.schenk.jrtrace.enginex.testscripts.EngineXDetailsTest;
import de.schenk.jrtrace.enginex.testscripts.EngineXDiagnosticsTest;
import de.schenk.jrtrace.enginex.testscripts.EngineXLifeCycleTest;
import de.schenk.jrtrace.helperagent.TypeCheckUtilTest;
import de.schenk.jrtrace.helperlib.tests.InspectUtilTest;
import de.schenk.jrtrace.helperlib.tests.ReflectionUtilTest;
import de.schenk.jrtrace.service.CommunicationLayerTest;
import de.schenk.jrtrace.service.JRTraceConnectToLaunchedAgentTest;
import de.schenk.jrtrace.service.JRTraceControllerTest;
import de.schenk.jrtrace.service.test.utils.JavaUtilTest;

@RunWith(Suite.class)
@SuiteClasses({ JRTraceConnectToLaunchedAgentTest.class,
		EngineXAnnotationReaderTest.class, EngineXLifeCycleTest.class,
		JRTraceControllerTest.class, JavaUtilTest.class, InspectUtilTest.class,
		EngineXDetailsTest.class, TypeCheckUtilTest.class,
		SimpleReinstallTest.class, ReflectionUtilTest.class,
		EngineXDiagnosticsTest.class, CommunicationLayerTest.class })
public class AllTests {

}
