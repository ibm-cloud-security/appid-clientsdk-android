package com.ibm.bluemix.appid.android.suite;

import com.ibm.bluemix.appid.android.AppIdTests;
import com.ibm.bluemix.appid.android.TokensTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({
		AppIdTests.class,
		TokensTest.class
})
public class AppIDTestSuite {}

