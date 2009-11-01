/**
 *  MicroEmulator
 *  Copyright (C) 2008 Bartek Teodorczyk <barteo@barteo.net>
 *  Copyright (C) 2008 Markus Heberling <markus@heberling.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 *
 *  @version $Id$
 */
package org.microemu.iphone;

import org.microemu.MIDletBridge;
import org.microemu.MIDletContext;
import org.microemu.RecordStoreManager;
import org.microemu.app.CommonInterface;
import org.microemu.app.launcher.Launcher;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.iphone.device.*;
import org.microemu.iphone.device.ui.AbstractUI;
import org.xmlvm.iphone.CGRect;
import org.xmlvm.iphone.UIApplication;
import org.xmlvm.iphone.UIScreen;
import org.xmlvm.iphone.UIWindow;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MicroEmulatorApplication extends UIApplication {

	public static void main(String[] args) throws Exception {
		// Main.main(new String[] { HelloJava.class.getName() });
        UIApplication.main(args, MicroEmulatorApplication.class);
	}

    @Override
    public void applicationDidFinishLaunching(UIApplication app) {
        new MicroEmulator().applicationDidFinishLaunching(app);

    }
}