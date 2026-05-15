package com.termux.tailscale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * TaskerEditActivity
 *
 * Shows a tiny picker (Connect / Disconnect / Toggle) when the user
 * configures this plugin inside Tasker or any Locale-compatible app.
 */
public class TaskerEditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple inline layout — no XML needed for something this small
        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        rg.setPadding(pad, pad, pad, pad);

        RadioButton rbUp   = makeRadio("Connect",    "up");
        RadioButton rbDown = makeRadio("Disconnect", "down");
        RadioButton rbTog  = makeRadio("Toggle",     "toggle");
        rbTog.setChecked(true);

        rg.addView(rbUp);
        rg.addView(rbDown);
        rg.addView(rbTog);

        Button save = new Button(this);
        save.setText("Save");
        save.setOnClickListener(v -> {
            String action = "toggle";
            int cid = rg.getCheckedRadioButtonId();
            if (cid == rbUp.getId())   action = "up";
            if (cid == rbDown.getId()) action = "down";

            Bundle b = new Bundle();
            b.putString("action", action);

            Intent result = new Intent();
            result.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", b);
            result.putExtra("com.twofortyfouram.locale.intent.extra.BLURB",
                "Tailscale: " + action);
            setResult(RESULT_OK, result);
            finish();
        });

        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.addView(rg);
        ll.addView(save);
        setContentView(ll);
    }

    private RadioButton makeRadio(String label, String tag) {
        RadioButton rb = new RadioButton(this);
        rb.setText(label);
        rb.setTag(tag);
        rb.setId(View.generateViewId());
        return rb;
    }
}
