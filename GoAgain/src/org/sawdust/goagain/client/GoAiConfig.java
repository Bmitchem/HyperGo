package org.sawdust.goagain.client;

import org.sawdust.goagain.shared.GoAI;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoAiConfig extends Widget {

  public static Widget getConfigWidget(final GoAI ai) {
    VerticalPanel verticalPanel = new VerticalPanel();
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Depth: "));
      IntegerBox v = new IntegerBox();
      v.setValue(ai.depth);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          ai.depth = event.getValue();
        }
      });
      verticalPanel.add(panel);
    }
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Bredth: "));
      IntegerBox v = new IntegerBox();
      v.setValue(ai.breadth);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          ai.breadth = event.getValue();
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
    
    return verticalPanel;
  }
}
