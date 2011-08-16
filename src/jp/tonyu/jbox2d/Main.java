package jp.tonyu.jbox2d;

import org.jbox2d.common.Vec2;
import org.jbox2d.pooling.arrays.Vec2Array;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.jbox2d.testbed.tests.DominoTest;

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


        TestbedModel model = new TestbedModel(new DominoTest());
        TestPanelJ2D panel = new TestPanelJ2D(this, model);
        addContentView(panel, new LinearLayout.LayoutParams
        		(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        //setContentView(panel);
        TestbedFrame testbed = new TestbedFrame(model, panel);
    }
}