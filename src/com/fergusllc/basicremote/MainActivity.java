/*
 * Copyright (C) 2010 Google Inc.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fergusllc.basicremote;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fergusllc.basicremote.R;
import com.fergusllc.basicremote.protocol.ICommandSender;
import com.google.anymote.Key.Code;

public class MainActivity extends BaseActivity {
	

	ICommandSender sender;
	
	public MainActivity() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
				
		super.onCreate(savedInstanceState);

		setContentView(R.layout.buttons);
		
		
		final Button buttonOne = (Button) findViewById(R.id.button_1);
		final Button buttonTwo = (Button) findViewById(R.id.button_2);
		final Button buttonThree = (Button) findViewById(R.id.button_3);

		buttonOne.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getCommands().keyPress(Code.KEYCODE_1);
			}
		});

	    buttonTwo.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
				getCommands().keyPress(Code.KEYCODE_2);
	        }
	    });
	    
	    buttonThree.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
				getCommands().keyPress(Code.KEYCODE_3);
	        }
	    });

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
