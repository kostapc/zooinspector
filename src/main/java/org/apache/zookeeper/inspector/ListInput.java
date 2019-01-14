package org.apache.zookeeper.inspector;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListInput extends JPanel implements ListSelectionListener, ActionListener {
  protected JLabel label;

  protected JTextField textfield;

  protected JList list;

  protected JScrollPane scrollPane;

  public ListInput(String[] data, String title) {
    setLayout(null);
    setBorder(new EmptyBorder(5, 5, 5, 5));
    label = new ListInputLabel(title, JLabel.LEFT);
    add(label);
    textfield = new ListInputText();
    textfield.addActionListener(this);
    label.setLabelFor(textfield); // NEW
    add(textfield);
    list = new ListInputList(data);
    list.setVisibleRowCount(4);
    list.addListSelectionListener(this);
    scrollPane = new JScrollPane(list);
    scrollPane.setVisible(false);
    add(scrollPane);

    textfield.addKeyListener(new KeyListener() {

      public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        scrollPane.setVisible(true);
      }


      public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

      }


      public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        // System.out.println("keyPressed: " + e.getKeyCode());
        switch (e.getKeyCode()) {
          case KeyEvent.VK_ENTER:
          case KeyEvent.VK_ESCAPE:
            scrollPane.setVisible(false);
            break;
          default:
            break;
        }

      }
    });
  }

  public ListInput(String title, int numCols) {
    setLayout(null);
    setBorder(new EmptyBorder(5, 5, 5, 5));
    label = new ListInputLabel(title, JLabel.LEFT);
    add(label);
    textfield = new ListInputText(numCols);
    textfield.addActionListener(this);
    label.setLabelFor(textfield); // NEW
    add(textfield);
    list = new ListInputList();
    list.setVisibleRowCount(4);
    list.addListSelectionListener(this);
    scrollPane = new JScrollPane(list);
    add(scrollPane);
  }


  public void setToolTipText(String text) {
    super.setToolTipText(text);
    label.setToolTipText(text);
    textfield.setToolTipText(text);
    list.setToolTipText(text);
  }

  public void setDisplayedMnemonic(char ch) {
    label.setDisplayedMnemonic(ch);
  }

  public void setSelected(String sel) {
    list.setSelectedValue(sel, true);
    textfield.setText(sel);
  }

  public String getSelected() {
    return textfield.getText();
  }

  public void setSelectedInt(int value) {
    setSelected(Integer.toString(value));
  }

  public int getSelectedInt() {
    try {
      return Integer.parseInt(getSelected());
    } catch (NumberFormatException ex) {
      return -1;
    }
  }


  public void valueChanged(ListSelectionEvent e) {
    Object obj = list.getSelectedValue();
    if (obj != null) {
      textfield.setText(obj.toString());
    }
  }


  public void actionPerformed(ActionEvent e) {
    ListModel model = list.getModel();
    String key = textfield.getText().toLowerCase();
    for (int i = 0; i < model.getSize(); i++) {
      String data = (String) model.getElementAt(i);
      if (data.toLowerCase().startsWith(key)) {
        list.setSelectedValue(data, true);
        break;
      }
    }
  }

  public void addListSelectionListener(ListSelectionListener lst) {
    list.addListSelectionListener(lst);
  }


  public Dimension getPreferredSize() {
    Insets ins = getInsets();
    Dimension labelSize = label.getPreferredSize();
    Dimension textfieldSize = textfield.getPreferredSize();
    Dimension scrollPaneSize = scrollPane.getPreferredSize();
    int w = Math.max(Math.max(labelSize.width, textfieldSize.width), scrollPaneSize.width);
    int h = labelSize.height + textfieldSize.height + scrollPaneSize.height;
    return new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom);
  }


  public Dimension getMaximumSize() {
    return getPreferredSize();
  }


  public Dimension getMinimumSize() {
    return getPreferredSize();
  }


  public void doLayout() {
    Insets ins = getInsets();
    Dimension size = getSize();
    int x = ins.left;
    int y = ins.top;
    int w = size.width - ins.left - ins.right;
    int h = size.height - ins.top - ins.bottom;

    Dimension labelSize = label.getPreferredSize();
    label.setBounds(x, y, w, labelSize.height);
    y += labelSize.height;
    Dimension textfieldSize = textfield.getPreferredSize();
    textfield.setBounds(x, y, w, textfieldSize.height);
    y += textfieldSize.height;
    scrollPane.setBounds(x, y, w, h - y);
  }

  public void appendResultSet(ResultSet results, int index, boolean toTitleCase) {
    textfield.setText("");
    DefaultListModel model = new DefaultListModel();
    try {
      while (results.next()) {
        String str = results.getString(index);
        if (toTitleCase) {
          str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }

        model.addElement(str);
      }
    } catch (SQLException ex) {
      System.err.println("appendResultSet: " + ex.toString());
    }
    list.setModel(model);
    if (model.getSize() > 0) {
      list.setSelectedIndex(0);
    }
  }

  class ListInputLabel extends JLabel {
    public ListInputLabel(String text, int alignment) {
      super(text, alignment);
    }


    public AccessibleContext getAccessibleContext() {
      return ListInput.this.getAccessibleContext();
    }
  }

  class ListInputText extends JTextField {
    public ListInputText() {}

    public ListInputText(int numCols) {
      super(numCols);
    }


    public AccessibleContext getAccessibleContext() {
      return ListInput.this.getAccessibleContext();
    }
  }

  class ListInputList extends JList {
    public ListInputList() {}

    public ListInputList(String[] data) {
      super(data);
    }


    public AccessibleContext getAccessibleContext() {
      return ListInput.this.getAccessibleContext();
    }
  }

  // Accessibility Support

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleOpenList();
    }
    return accessibleContext;
  }

  protected class AccessibleOpenList extends AccessibleJComponent {


    public String getAccessibleName() {
      System.out.println("getAccessibleName: " + accessibleName);
      if (accessibleName != null) {
        return accessibleName;
      }
      return label.getText();
    }


    public AccessibleRole getAccessibleRole() {
      return AccessibleRole.LIST;
    }
  }

  public static void main(String[] a) {
    String[] fontNames = new String[]{"Roman", "Times Roman"};
    ListInput lstFontName = new ListInput(fontNames, "Name:");
    lstFontName.setDisplayedMnemonic('n');
    lstFontName.setToolTipText("Font name");
    JFrame f = new JFrame();
    f.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.getContentPane().add(lstFontName);

    f.pack();
    f.setSize(new Dimension(300, 200));
    f.setVisible(true);

  }

}
