package jp.tonyu.jbox2d;

import org.jbox2d.common.Vec2;
import org.jbox2d.pooling.arrays.Vec2Array;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.android.TestPanelAndroid;
import org.jbox2d.testbed.tests.DominoTest;
import org.jbox2d.testbed.tests.SphereStack;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);


        TestbedModel model = new TestbedModel(new SphereStack());
        TestPanelAndroid panel = new TestPanelAndroid(this, model);
        addContentView(panel, new LinearLayout.LayoutParams
        		(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        //setContentView(panel);
        TestbedFrame testbed = new TestbedFrame(model, panel);
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	TestbedController.active=false;
    }
    @Override
    protected void onDestroy() {
    	TestbedController.active=false;
    	super.onDestroy();
    }
    @Override
    protected void onResume() {
    	TestbedController.active=true;
    	super.onResume();
    }
    @Override
    protected void onStart() {
    	TestbedController.active=true;
    	super.onStart();
    }
    @Override
    protected void onRestart() {
    	TestbedController.active=false;
    	super.onRestart();
    }
    @Override
    protected void onStop() {
    	TestbedController.active=false;
    	super.onStop();
    }
}