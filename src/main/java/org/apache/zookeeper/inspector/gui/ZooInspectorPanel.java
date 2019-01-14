/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zookeeper.inspector.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import net.c0f3.zookeeper.ScreenWrapper;
import net.c0f3.zookeeper.ZKTreeExportDialogForm;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.inspector.gui.nodeviewer.ZooInspectorNodeViewer;
import org.apache.zookeeper.inspector.logger.LoggerFactory;
import org.apache.zookeeper.inspector.manager.ZooInspectorManager;

/**
 * The parent {@link JPanel} for the whole application
 */
public class ZooInspectorPanel extends JPanel implements NodeViewersChangeListener {
    private final JButton refreshButton;
    private final JButton disconnectButton;
    private final JButton connectButton;
    private final ZooInspectorNodeViewersPanel nodeViewersPanel;
    private final ZooInspectorTreeViewer treeViewer;
    private final ZooInspectorManager zooInspectorManager;
    private final JButton addNodeButton;
    private final JButton deleteNodeButton;
    private final JButton nodeViewersButton;
    private final JButton aboutButton;
    private final JButton exportButton;
    private final HostStatusLabel  connectedHost;
    private final List<NodeViewersChangeListener> listeners = new ArrayList<NodeViewersChangeListener>();
    {
        listeners.add(this);
    }

    public boolean checkZookeeperStates(String info) {
      if (zooInspectorManager == null
          || zooInspectorManager.getZookeeperStates() != States.CONNECTED) {
        refreshButton.setEnabled(false);
        addNodeButton.setEnabled(false);
        deleteNodeButton.setEnabled(false);
        JOptionPane
        .showMessageDialog(
                ZooInspectorPanel.this,
                info,
                "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      return true;
    }

    /**
     * @param zooInspectorManager
     *            - the {@link ZooInspectorManager} for the application
     */
    public ZooInspectorPanel(final ZooInspectorManager zooInspectorManager) {
        this.zooInspectorManager = zooInspectorManager;
        final ArrayList<ZooInspectorNodeViewer> nodeViewers = new ArrayList<ZooInspectorNodeViewer>();
        try {
            List<String> defaultNodeViewersClassNames = this.zooInspectorManager
                    .getDefaultNodeViewerConfiguration();
            for (String className : defaultNodeViewersClassNames) {
                nodeViewers.add((ZooInspectorNodeViewer) Class.forName(
                        className).newInstance());
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger().error(
                    "Error loading default node viewers.", ex);
            JOptionPane.showMessageDialog(ZooInspectorPanel.this,
                    "Error loading default node viewers: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        nodeViewersPanel = new ZooInspectorNodeViewersPanel(zooInspectorManager, nodeViewers);
        treeViewer = new ZooInspectorTreeViewer(
            ZooInspectorPanel.this,
            zooInspectorManager,
            nodeViewersPanel
        );
        this.setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        connectButton = new JButton(ZooInspectorIconResources.getConnectIcon());
        disconnectButton = new JButton(ZooInspectorIconResources.getDisconnectIcon());
        refreshButton = new JButton(ZooInspectorIconResources.getRefreshIcon());
        addNodeButton = new JButton(ZooInspectorIconResources.getAddNodeIcon());
        deleteNodeButton = new JButton(ZooInspectorIconResources.getDeleteNodeIcon());
        nodeViewersButton = new JButton(ZooInspectorIconResources.getChangeNodeViewersIcon());
        exportButton = new JButton(ZooInspectorIconResources.getSaveIcon());
        aboutButton = new JButton(ZooInspectorIconResources.getInformationIcon());
        connectedHost = new HostStatusLabel();
        toolbar.add(connectButton);
        toolbar.add(disconnectButton);
        toolbar.add(refreshButton);
        toolbar.add(addNodeButton);
        toolbar.add(deleteNodeButton);
        toolbar.add(nodeViewersButton);
        toolbar.add(exportButton);
        toolbar.add(aboutButton);
        toolbar.add(connectedHost);
        aboutButton.setEnabled(true);
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        refreshButton.setEnabled(false);
        addNodeButton.setEnabled(false);
        deleteNodeButton.setEnabled(false);
        nodeViewersButton.setEnabled(true);
        exportButton.setEnabled(false);
        exportButton.setToolTipText("export to .zk file");
        nodeViewersButton.setToolTipText("Change Node Viewers");
        aboutButton.setToolTipText("About ZooInspector");
        connectButton.setToolTipText("Connect");
        disconnectButton.setToolTipText("Disconnect");
        refreshButton.setToolTipText("Refresh");
        addNodeButton.setToolTipText("Add Node");
        deleteNodeButton.setToolTipText("Delete Node");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ZooInspectorConnectionPropertiesDialog zicpd = new ZooInspectorConnectionPropertiesDialog(
                        zooInspectorManager.getLastConnectionProps(),
                        zooInspectorManager.getConnectionPropertiesTemplate(),
                        ZooInspectorPanel.this);
                Point position = ScreenWrapper.getWindowPosition(zicpd.getWidth(), zicpd.getHeight());
                zicpd.setBounds(position.x, position.y, zicpd.getWidth(), zicpd.getHeight());
                zicpd.setVisible(true);
            }
        });
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treeViewer.refreshView();
            }
        });
        addNodeButton.addActionListener(new ActionListener() {
            String nodeName;
            @Override
            public void actionPerformed(ActionEvent e) {

                final List<String> selectedNodes = treeViewer
                        .getSelectedNodes();
                if (selectedNodes.size() == 1) {
                    nodeName = JOptionPane.showInputDialog(
                            ZooInspectorPanel.this,
                            "Please Enter a name for the new node",
                            "Create Node", JOptionPane.INFORMATION_MESSAGE);
                    if (nodeName != null && nodeName.length() > 0) {
                        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

                            @Override
                            protected Boolean doInBackground() throws Exception {
                                return ZooInspectorPanel.this.zooInspectorManager
                                        .createNode(selectedNodes.get(0),
                                                nodeName);
                            }

                            @Override
                            protected void done() {
//                                treeViewer.refreshView();
                              treeViewer.refreshViewAfterAdd(selectedNodes.get(0), nodeName);
                            }
                        };
                        worker.execute();
                    }
                } else {
                    JOptionPane.showMessageDialog(ZooInspectorPanel.this,
                            "Please select 1 parent node for the new node.");
                }
            }
        });
        deleteNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final List<String> selectedNodes = treeViewer
                        .getSelectedNodes();
                if (selectedNodes.size() == 0) {
                    JOptionPane.showMessageDialog(ZooInspectorPanel.this,
                            "Please select at least 1 node to be deleted");
                } else {
                    int answer = JOptionPane.showConfirmDialog(
                            ZooInspectorPanel.this,
                            "Are you sure you want to delete the selected nodes?"
                                    + "(This action cannot be reverted)",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

                            @Override
                            protected Boolean doInBackground() throws Exception {
                                for (String nodePath : selectedNodes) {
                                    ZooInspectorPanel.this.zooInspectorManager
                                            .deleteNode(nodePath);
                                }
                                return true;
                            }

                            @Override
                            protected void done() {
                              //treeViewer.refreshView();
                              treeViewer.refreshViewAfterDelete(selectedNodes);
                            }
                        };
                        worker.execute();
                    }
                }
            }
        });
        nodeViewersButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ZooInspectorNodeViewersDialog nvd = new ZooInspectorNodeViewersDialog(
                        JOptionPane.getRootFrame(), nodeViewers, listeners,
                        zooInspectorManager);
                nvd.setVisible(true);
            }
        });
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ZooInspectorAboutDialog zicpd = new ZooInspectorAboutDialog(
                        JOptionPane.getRootFrame());
                zicpd.setVisible(true);
            }
        });
        exportButton.addActionListener((a)->{
            ZKTreeExportDialogForm exportDialogForm = new ZKTreeExportDialogForm(
                zooInspectorManager.getLastConnectionProps().get(ZooInspectorManager.CONNECT_STRING).toString()
            );
            exportDialogForm.pack();
            Point position = ScreenWrapper.getWindowPosition(
                exportDialogForm.getWidth(),
                exportDialogForm.getHeight()
            );
            exportDialogForm.setBounds(
                position.x, position.y,
                exportDialogForm.getWidth(), exportDialogForm.getHeight()
            );
            exportDialogForm.setVisible(true);
        });

        JScrollPane treeScroller = new JScrollPane(treeViewer);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treeScroller, nodeViewersPanel);
        splitPane.setResizeWeight(0.25);
        this.add(splitPane, BorderLayout.CENTER);
        this.add(toolbar, BorderLayout.NORTH);
    }

    /**
     * @param connectionProps
     *            the {@link Properties} for connecting to the zookeeper
     *            instance
     */
    public void connect(final Properties connectionProps) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                zooInspectorManager.setLastConnectionProps(connectionProps);
                return zooInspectorManager.connect(connectionProps, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if(event.getState() == Event.KeeperState.Disconnected){
                            treeViewer.refreshView();
                            connectButton.setEnabled(true);
                            disconnectButton.setEnabled(false);
                            refreshButton.setEnabled(false);
                            addNodeButton.setEnabled(false);
                            deleteNodeButton.setEnabled(false);
                            nodeViewersPanel.setEnabled(false);
                            connectedHost.disconnected();
                        }
                    }
                });
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        // connect successfully
                        treeViewer.refreshView();
                        connectButton.setEnabled(false);
                        disconnectButton.setEnabled(true);
                        refreshButton.setEnabled(true);
                        addNodeButton.setEnabled(true);
                        deleteNodeButton.setEnabled(true);
                        exportButton.setEnabled(true);
                        connectedHost.connected(
                            Optional.ofNullable(connectionProps.get(ZooInspectorManager.CONNECT_STRING)).orElse("<unknown host>").toString()
                        );
                        // save successful connect string in default properties
                        zooInspectorManager.updateDefaultConnectionFile(connectionProps);
                    } else {
                        JOptionPane.showMessageDialog(ZooInspectorPanel.this,
                                "Unable to connect to zookeeper", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LoggerFactory
                            .getLogger()
                            .error(
                                    "Error occurred while connecting to ZooKeeper server",
                                    e);
                } catch (IOException e) {
                    LoggerFactory
                      .getLogger()
                      .error("Error occurred while update default connections", e);
                }
            }

        };
        worker.execute();
    }

    /**
	 *
	 */
    public void disconnect() {
        disconnect(false);
    }

    /**
     * @param wait
     *            - set this to true if the method should only return once the
     *            application has successfully disconnected
     */
    public void disconnect(boolean wait) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                return ZooInspectorPanel.this.zooInspectorManager.disconnect();
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        treeViewer.clearView();
                        connectButton.setEnabled(true);
                        disconnectButton.setEnabled(false);
                        refreshButton.setEnabled(false);
                        addNodeButton.setEnabled(false);
                        deleteNodeButton.setEnabled(false);
                        connectedHost.disconnected();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LoggerFactory
                            .getLogger()
                            .error(
                                    "Error occurred while disconnecting from ZooKeeper server",
                                    e);
                }
            }

        };
        worker.execute();
        if (wait) {
            while (!worker.isDone()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LoggerFactory
                            .getLogger()
                            .error(
                                    "Error occurred while disconnecting from ZooKeeper server",
                                    e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.apache.zookeeper.inspector.gui.NodeViewersChangeListener#
     * nodeViewersChanged(java.util.List)
     */
    @Override
    public void nodeViewersChanged(List<ZooInspectorNodeViewer> newViewers) {
        this.nodeViewersPanel.setNodeViewers(newViewers);
    }

    /**
     * @param connectionProps
     * @throws IOException
     */
    public void setdefaultConnectionProps(Properties connectionProps)
            throws IOException {
        this.zooInspectorManager.saveDefaultConnectionFile(connectionProps);
    }
}
