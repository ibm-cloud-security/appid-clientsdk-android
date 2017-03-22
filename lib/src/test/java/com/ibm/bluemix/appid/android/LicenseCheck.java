/*
	Copyright 2017 IBM Corp.
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.ibm.bluemix.appid.android;

import junit.framework.Assert;

import org.junit.Test;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rotembr on 22/03/2017.
 */

public class LicenseCheck {
    private static final String LICENSE = "/*\n\tCopyright 2017 IBM Corp.\n\tLicensed under the Apache License, Version 2.0 (the \"License\");\n\tyou may not use this file except in compliance with the License.\n\tYou may obtain a copy of the License at\n\thttp://www.apache.org/licenses/LICENSE-2.0\n\tUnless required by applicable law or agreed to in writing, software\n\tdistributed under the License is distributed on an \"AS IS\" BASIS,\n\tWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n\tSee the License for the specific language governing permissions and\n\tlimitations under the License.\n*/";

    @Test
    public void testLicensesHeaders(){
        try {
            List<String> missingLicenseFiles = new ArrayList<>();
            File sourceDir = new File(new File("").getAbsolutePath() + "/lib/src/main/java/com/ibm/");
            File sourceTestDir = new File(new File("").getAbsolutePath() + "/lib/src/test/java/com/ibm/");
            List<File> sourceFiles = getListFiles(sourceDir);
            List<File> testsFiles = getListFiles(sourceTestDir);
            sourceFiles.addAll(testsFiles);
            for(File file : sourceFiles) {
                FileInputStream fisTargetFile = new FileInputStream(file);
                String targetFileStr = IOUtils.toString(fisTargetFile, "UTF-8");
                if(!targetFileStr.startsWith(LICENSE)){
                    missingLicenseFiles.add(file.getPath());
                }
            }
            Assert.assertTrue("The following files missing the IBM License header: " + missingLicenseFiles.toString(),missingLicenseFiles.isEmpty());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".java")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

}
