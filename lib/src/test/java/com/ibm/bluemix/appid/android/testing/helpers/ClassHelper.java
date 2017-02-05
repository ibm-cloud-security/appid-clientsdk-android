package com.ibm.bluemix.appid.android.testing.helpers;

import static org.assertj.core.api.Java6Assertions.*;

public class ClassHelper {
	public static void assertSame (Object o, Class c){
		assertThat(o.getClass().getName()).isEqualTo(c.getName());
	}
}
