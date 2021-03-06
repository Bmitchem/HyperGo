package org.sawdust.goagain.client;

import org.sawdust.goagain.shared.go.ai.GoAI;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoAiConfig extends Widget {

  public static Widget getConfigWidget(final GoAI ai) {
    VerticalPanel verticalPanel = new VerticalPanel();
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Depth: "));
      final TextBox v = new TextBox();
      v.setValue(Integer.toString(ai.depth));
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<String>() {
        public void onValueChange(ValueChangeEvent<String> event) {
          try {
            ai.depth = Integer.parseInt(event.getValue());
          } catch (NumberFormatException e) {
            v.setValue(Integer.toString(ai.depth));
          }
        }
      });
      verticalPanel.add(panel);
    }
    
    {
      CheckBox v = new CheckBox("Use Server ");
      v.setValue(ai.useServer);
      v.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          ai.useServer = event.getValue();
        }
      });
      verticalPanel.add(v);
    }
    
    {
      CheckBox v = new CheckBox("Experimental ");
      v.setValue(ai.useExperiment);
      v.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          ai.useExperiment = event.getValue();
        }
      });
      verticalPanel.add(v);
    }
    
    return verticalPanel;
  }
}
