package org.sawdust.goagain.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Util {

  public static DialogBox showDialog(Label... widgets) {
    final DialogBox dialogBox = new DialogBox();
    VerticalPanel w = new VerticalPanel();
    for (Label widget : widgets)
      w.add(widget);
    Button close = new Button("Close");
    close.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    w.add(close);
    dialogBox.add(w);
    dialogBox.center();
    dialogBox.show();
    return dialogBox;
  }

}
