/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.schenk.jrtrace.enginex.reinstall.ReinstallStressTest;
import de.schenk.jrtrace.enginex.testscripts.StressTest;

@RunWith(Suite.class)
@SuiteClasses({ StressTest.class, ReinstallStressTest.class })
public class AllHeavyLoadTests {

}
