package fansirsqi.xposed.sesame.model.modelFieldExt;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fansirsqi.xposed.sesame.R;
import fansirsqi.xposed.sesame.model.ModelField;
import fansirsqi.xposed.sesame.ui.StringDialog;

public class TextModelField extends ModelField<String> {

    public TextModelField(String code, String name, String value) {
        super(code, name, value);
    }

    @Override
    public String getType() {
        return "TEXT";
    }

    @Override
    public String getConfigValue() {
        return value;
    }

    @Override
    public void setConfigValue(String configValue) {
        value = configValue;
    }

    @JsonIgnore
    public View getView(Context context) {
        Button btn = new Button(context);
        btn.setText(getName());
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setTextColor(ContextCompat.getColor(context, R.color.button));
        btn.setBackground(ContextCompat.getDrawable(context, R.drawable.button));
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setMinHeight(150);
        btn.setMaxHeight(180);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> StringDialog.showReadDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

}
