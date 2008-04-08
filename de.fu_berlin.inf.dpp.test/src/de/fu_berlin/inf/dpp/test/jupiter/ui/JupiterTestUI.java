package de.fu_berlin.inf.dpp.test.jupiter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.jupiter.Operation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument2;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterDocumentListener;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument2;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;

/**
 * 
 * @author troll
 */
public class JupiterTestUI extends javax.swing.JPanel {

	private static Logger logger = Logger.getLogger(JupiterTestUI.class);

	public static String SIDE_1 = "side1";
	public static String SIDE_2 = "side2";
	public static String SIDE_3 = "side3";

	Messages resourceMap = new Messages();
	ActionListener actionListener;

	JupiterSimulater jupiter;

	/** Creates new form NewJPanel */
	public JupiterTestUI() {
		actionListener = new JupiterActionListener();
		initComponents();
		initListener();
		jupiter = new JupiterSimulater();
		
		jupiter.deactiveSide3();
	}

	public void update() {
		this.repaint();
	}

	private void initComponents() {

		jLabelInitialState = new javax.swing.JLabel();
		jTextFieldInitialState = new javax.swing.JTextField();
		jLabelGlobalResult = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		jRadioButtonInsert = new javax.swing.JRadioButton();
		jRadioButtonDelete = new javax.swing.JRadioButton();
		jTextFieldPosition = new javax.swing.JTextField();
		jLabelPositionSide1 = new javax.swing.JLabel();
		jTextFieldDelay = new javax.swing.JTextField();
		jLabelDelaySide1 = new javax.swing.JLabel();
		jTextFieldTextSide1 = new javax.swing.JTextField();
		jLabelTextSide1 = new javax.swing.JLabel();
		jTextFieldResultSide1 = new javax.swing.JTextField();
		jButtonExecuteSide1 = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jRadioButtonInsertSide2 = new javax.swing.JRadioButton();
		jRadioButtonDeleteSide2 = new javax.swing.JRadioButton();
		jTextFieldPositionSide2 = new javax.swing.JTextField();
		jLabelPositionSide2 = new javax.swing.JLabel();
		jTextFieldDelaySide2 = new javax.swing.JTextField();
		jLabelDelaySide2 = new javax.swing.JLabel();
		jTextFieldTextSide2 = new javax.swing.JTextField();
		jLabelTextSide2 = new javax.swing.JLabel();
		jTextFieldResultSide2 = new javax.swing.JTextField();
		jButtonExecuteSide2 = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jRadioButtonInsertSide3 = new javax.swing.JRadioButton();
		jRadioButtonDeleteSide3 = new javax.swing.JRadioButton();
		jTextFieldPositionSide3 = new javax.swing.JTextField();
		jLabelPositionSide3 = new javax.swing.JLabel();
		jTextFieldDelaySide3 = new javax.swing.JTextField();
		jLabelDelaySide3 = new javax.swing.JLabel();
		jTextFieldTextSide3 = new javax.swing.JTextField();
		jLabelTextSide3 = new javax.swing.JLabel();
		jTextFieldResultSide3 = new javax.swing.JTextField();
		jButtonExecuteSide3 = new javax.swing.JButton();
		jButtonRun = new javax.swing.JButton();
		jButtonExit = new javax.swing.JButton();

		setName("Form"); // NOI18N

		// org.jdesktop.application.ResourceMap resourceMap =
		// org.jdesktop.application.Application.getInstance(desktopapplication1.DesktopApplication1.class).getContext().getResourceMap(NewJPanel.class);
		jLabelInitialState.setText(resourceMap
				.getString("jLabelInitialState.text")); // NOI18N
		jLabelInitialState.setName("jLabelInitialState"); // NOI18N

		jTextFieldInitialState.setName("jTextFieldInitialState"); // NOI18N

		// jLabelGlobalResult.setFont(resourceMap.getFont("jLabelGlobalResult.font"));
		// // NOI18N
		jLabelGlobalResult.setText(resourceMap
				.getString("jLabelGlobalResult.text")); // NOI18N
		jLabelGlobalResult.setName("jLabelGlobalResult"); // NOI18N

		jPanel1.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Side 1"));
		jPanel1.setName("jPanel1"); // NOI18N

		jRadioButtonInsert.setText(resourceMap
				.getString("jRadioButtonInsert.text")); // NOI18N
		jRadioButtonInsert.setName("jRadioButtonInsert"); // NOI18N
		// jRadioButtonInsert.setSelected(true);

		jRadioButtonDelete.setText(resourceMap
				.getString("jRadioButtonDelete.text")); // NOI18N
		jRadioButtonDelete.setName("jRadioButtonDelete"); // NOI18N

		jTextFieldPosition.setName("jTextFieldPosition"); // NOI18N

		jLabelPositionSide1.setText(resourceMap
				.getString("jLabelPositionSide1.text")); // NOI18N
		jLabelPositionSide1.setName("jLabelPositionSide1"); // NOI18N

		jTextFieldDelay.setText(resourceMap.getString("jTextFieldDelay.text")); // NOI18N
		jTextFieldDelay.setName("jTextFieldDelay"); // NOI18N

		jLabelDelaySide1
				.setText(resourceMap.getString("jLabelDelaySide1.text")); // NOI18N
		jLabelDelaySide1.setName("jLabelDelaySide1"); // NOI18N

		jTextFieldTextSide1.setName("jTextFieldTextSide1"); // NOI18N

		jLabelTextSide1.setText(resourceMap.getString("jLabelTextSide1.text")); // NOI18N
		jLabelTextSide1.setName("jLabelTextSide1"); // NOI18N

		jTextFieldResultSide1.setName("jTextFieldResultSide1"); // NOI18N

		jButtonExecuteSide1.setText(resourceMap
				.getString("jButtonExecuteSide1.text")); // NOI18N
		jButtonExecuteSide1.setName("jButtonExecuteSide1"); // NOI18N

		org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel1Layout
										.createSequentialGroup()
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																jPanel1Layout
																		.createSequentialGroup()
																		.add(
																				8,
																				8,
																				8)
																		.add(
																				jRadioButtonInsert)
																		.add(
																				18,
																				18,
																				18)
																		.add(
																				jPanel1Layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								jPanel1Layout
																										.createSequentialGroup()
																										.add(
																												jLabelPositionSide1)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.UNRELATED)
																										.add(
																												jLabelDelaySide1))
																						.add(
																								jPanel1Layout
																										.createSequentialGroup()
																										.add(
																												12,
																												12,
																												12)
																										.add(
																												jPanel1Layout
																														.createParallelGroup(
																																org.jdesktop.layout.GroupLayout.LEADING)
																														.add(
																																jTextFieldResultSide1,
																																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																251,
																																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																														.add(
																																jPanel1Layout
																																		.createSequentialGroup()
																																		.add(
																																				jTextFieldPosition,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																				22,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																																		.add(
																																				18,
																																				18,
																																				18)
																																		.add(
																																				jTextFieldDelay,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																				43,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																																		.add(
																																				18,
																																				18,
																																				18)
																																		.add(
																																				jPanel1Layout
																																						.createParallelGroup(
																																								org.jdesktop.layout.GroupLayout.LEADING)
																																						.add(
																																								jLabelTextSide1)
																																						.add(
																																								jTextFieldTextSide1,
																																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																								150,
																																								Short.MAX_VALUE))))))
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED))
														.add(
																jPanel1Layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				jRadioButtonDelete)
																		.add(
																				285,
																				285,
																				285)))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jButtonExecuteSide1)
										.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel1Layout
										.createSequentialGroup()
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jLabelPositionSide1)
														.add(jLabelDelaySide1)
														.add(jLabelTextSide1))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jRadioButtonInsert)
														.add(
																jTextFieldPosition,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jTextFieldDelay,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jTextFieldTextSide1,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(
												jPanel1Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jRadioButtonDelete)
														.add(
																jTextFieldResultSide1,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(12, Short.MAX_VALUE))
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								jPanel1Layout.createSequentialGroup()
										.addContainerGap(46, Short.MAX_VALUE)
										.add(jButtonExecuteSide1)
										.addContainerGap()));

		jPanel2.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Side 2"));
		jPanel2.setName("jPanel2"); // NOI18N

		jRadioButtonInsertSide2.setText(resourceMap
				.getString("jRadioButtonInsertSide2.text")); // NOI18N
		jRadioButtonInsertSide2.setName("jRadioButtonInsertSide2"); // NOI18N
		// jRadioButtonInsertSide2.setSelected(true);

		jRadioButtonDeleteSide2.setText(resourceMap
				.getString("jRadioButtonDeleteSide2.text")); // NOI18N
		jRadioButtonDeleteSide2.setName("jRadioButtonDeleteSide2"); // NOI18N

		jTextFieldPositionSide2.setName("jTextFieldPositionSide2"); // NOI18N

		jLabelPositionSide2.setText(resourceMap
				.getString("jLabelPositionSide2.text")); // NOI18N
		jLabelPositionSide2.setName("jLabelPositionSide2"); // NOI18N

		jTextFieldDelaySide2.setText(resourceMap
				.getString("jTextFieldDelaySide2.text")); // NOI18N
		jTextFieldDelaySide2.setName("jTextFieldDelaySide2"); // NOI18N

		jLabelDelaySide2
				.setText(resourceMap.getString("jLabelDelaySide2.text")); // NOI18N
		jLabelDelaySide2.setName("jLabelDelaySide2"); // NOI18N

		jTextFieldTextSide2.setName("jTextFieldTextSide2"); // NOI18N

		jLabelTextSide2.setText(resourceMap.getString("jLabelTextSide2.text")); // NOI18N
		jLabelTextSide2.setName("jLabelTextSide2"); // NOI18N

		jTextFieldResultSide2.setName("jTextFieldResultSide2"); // NOI18N

		jButtonExecuteSide2.setText(resourceMap
				.getString("jButtonExecuteSide2.text")); // NOI18N
		jButtonExecuteSide2.setName("jButtonExecuteSide2"); // NOI18N

		org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(
				jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(jPanel2Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel2Layout
										.createSequentialGroup()
										.add(
												jPanel2Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																jPanel2Layout
																		.createSequentialGroup()
																		.add(
																				8,
																				8,
																				8)
																		.add(
																				jRadioButtonInsertSide2)
																		.add(
																				18,
																				18,
																				18)
																		.add(
																				jPanel2Layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								jPanel2Layout
																										.createSequentialGroup()
																										.add(
																												jLabelPositionSide2)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.UNRELATED)
																										.add(
																												jLabelDelaySide2))
																						.add(
																								jPanel2Layout
																										.createSequentialGroup()
																										.add(
																												12,
																												12,
																												12)
																										.add(
																												jPanel2Layout
																														.createParallelGroup(
																																org.jdesktop.layout.GroupLayout.LEADING)
																														.add(
																																jTextFieldResultSide2,
																																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																251,
																																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																														.add(
																																jPanel2Layout
																																		.createSequentialGroup()
																																		.add(
																																				jTextFieldPositionSide2,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																				22,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																																		.add(
																																				18,
																																				18,
																																				18)
																																		.add(
																																				jTextFieldDelaySide2,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																				43,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																																		.add(
																																				18,
																																				18,
																																				18)
																																		.add(
																																				jPanel2Layout
																																						.createParallelGroup(
																																								org.jdesktop.layout.GroupLayout.LEADING)
																																						.add(
																																								jLabelTextSide2)
																																						.add(
																																								jTextFieldTextSide2,
																																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																								150,
																																								Short.MAX_VALUE))))))
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED))
														.add(
																jPanel2Layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				jRadioButtonDeleteSide2)
																		.add(
																				285,
																				285,
																				285)))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jButtonExecuteSide2)
										.addContainerGap()));
		jPanel2Layout
				.setVerticalGroup(jPanel2Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel2Layout
										.createSequentialGroup()
										.add(
												jPanel2Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jLabelPositionSide2)
														.add(jLabelDelaySide2)
														.add(jLabelTextSide2))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel2Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jRadioButtonInsertSide2)
														.add(
																jTextFieldPositionSide2,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jTextFieldDelaySide2,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jTextFieldTextSide2,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(
												jPanel2Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jRadioButtonDeleteSide2)
														.add(
																jTextFieldResultSide2,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jButtonExecuteSide2))
										.addContainerGap(
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jPanel3.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Side 3"));
		jPanel3.setName("jPanel3"); // NOI18N

		jRadioButtonInsertSide3.setText(resourceMap
				.getString("jRadioButtonInsertSide3.text")); // NOI18N
		jRadioButtonInsertSide3.setName("jRadioButtonInsertSide3"); // NOI18N
		// jRadioButtonInsertSide3.setSelected(true);

		jRadioButtonDeleteSide3.setText(resourceMap
				.getString("jRadioButtonDeleteSide3.text")); // NOI18N
		jRadioButtonDeleteSide3.setName("jRadioButtonDeleteSide3"); // NOI18N

		jTextFieldPositionSide3.setName("jTextFieldPositionSide3"); // NOI18N

		jLabelPositionSide3.setText(resourceMap
				.getString("jLabelPositionSide3.text")); // NOI18N
		jLabelPositionSide3.setName("jLabelPositionSide3"); // NOI18N

		jTextFieldDelaySide3.setText(resourceMap
				.getString("jTextFieldDelaySide3.text")); // NOI18N
		jTextFieldDelaySide3.setName("jTextFieldDelaySide3"); // NOI18N

		jLabelDelaySide3
				.setText(resourceMap.getString("jLabelDelaySide3.text")); // NOI18N
		jLabelDelaySide3.setName("jLabelDelaySide3"); // NOI18N

		jTextFieldTextSide3.setName("jTextFieldTextSide3"); // NOI18N

		jLabelTextSide3.setText(resourceMap.getString("jLabelTextSide3.text")); // NOI18N
		jLabelTextSide3.setName("jLabelTextSide3"); // NOI18N

		jTextFieldResultSide3.setName("jTextFieldResultSide3"); // NOI18N

		jButtonExecuteSide3.setText(resourceMap
				.getString("jButtonExecuteSide3.text")); // NOI18N
		jButtonExecuteSide3.setName("jButtonExecuteSide3"); // NOI18N

		org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(
				jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout
				.setHorizontalGroup(jPanel3Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel3Layout
										.createSequentialGroup()
										.add(
												jPanel3Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																jPanel3Layout
																		.createSequentialGroup()
																		.add(
																				8,
																				8,
																				8)
																		.add(
																				jRadioButtonInsertSide3)
																		.add(
																				18,
																				18,
																				18)
																		.add(
																				jPanel3Layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								jPanel3Layout
																										.createSequentialGroup()
																										.add(
																												jLabelPositionSide3)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.UNRELATED)
																										.add(
																												jLabelDelaySide3))
																						.add(
																								jPanel3Layout
																										.createSequentialGroup()
																										.add(
																												12,
																												12,
																												12)
																										.add(
																												jPanel3Layout
																														.createParallelGroup(
																																org.jdesktop.layout.GroupLayout.LEADING)
																														.add(
																																jTextFieldResultSide3,
																																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																251,
																																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																														.add(
																																jPanel3Layout
																																		.createSequentialGroup()
																																		.add(
																																				jTextFieldPositionSide3,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																				22,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																																		.add(
																																				18,
																																				18,
																																				18)
																																		.add(
																																				jTextFieldDelaySide3,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																				43,
																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																																		.add(
																																				18,
																																				18,
																																				18)
																																		.add(
																																				jPanel3Layout
																																						.createParallelGroup(
																																								org.jdesktop.layout.GroupLayout.LEADING)
																																						.add(
																																								jLabelTextSide3)
																																						.add(
																																								jTextFieldTextSide3,
																																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																								150,
																																								Short.MAX_VALUE))))))
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED))
														.add(
																jPanel3Layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				jRadioButtonDeleteSide3)
																		.add(
																				285,
																				285,
																				285)))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jButtonExecuteSide3)
										.addContainerGap()));
		jPanel3Layout
				.setVerticalGroup(jPanel3Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel3Layout
										.createSequentialGroup()
										.add(
												jPanel3Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jLabelPositionSide3)
														.add(jLabelDelaySide3)
														.add(jLabelTextSide3))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jPanel3Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jRadioButtonInsertSide3)
														.add(
																jTextFieldPositionSide3,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jTextFieldDelaySide3,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																jTextFieldTextSide3,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(
												jPanel3Layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																jRadioButtonDeleteSide3)
														.add(
																jTextFieldResultSide3,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(12, Short.MAX_VALUE))
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								jPanel3Layout.createSequentialGroup()
										.addContainerGap(46, Short.MAX_VALUE)
										.add(jButtonExecuteSide3)
										.addContainerGap()));

		jButtonRun.setText(resourceMap.getString("jButtonRun.text")); // NOI18N
		jButtonRun.setName("jButtonRun"); // NOI18N

		jButtonExit.setText(resourceMap.getString("jButtonExit.text")); // NOI18N
		jButtonExit.setName("jButtonExit"); // NOI18N

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				this);
		this.setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								layout
										.createSequentialGroup()
										.addContainerGap(320, Short.MAX_VALUE)
										.add(jButtonRun)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jButtonExit).addContainerGap())
						.add(
								layout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.LEADING)
										.add(
												layout
														.createSequentialGroup()
														.add(3, 3, 3)
														.add(
																layout
																		.createParallelGroup(
																				org.jdesktop.layout.GroupLayout.LEADING)
																		.add(
																				jPanel3,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																		.add(
																				layout
																						.createSequentialGroup()
																						.add(
																								48,
																								48,
																								48)
																						.add(
																								jLabelInitialState)
																						.addPreferredGap(
																								org.jdesktop.layout.LayoutStyle.RELATED)
																						.add(
																								jTextFieldInitialState,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								140,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																						.add(
																								43,
																								43,
																								43)
																						.add(
																								jLabelGlobalResult))
																		.add(
																				jPanel1,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																		.add(
																				jPanel2,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
														.addContainerGap(
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								layout
										.createSequentialGroup()
										.addContainerGap(376, Short.MAX_VALUE)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jButtonRun).add(
																jButtonExit))
										.addContainerGap())
						.add(
								layout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.LEADING)
										.add(
												layout
														.createSequentialGroup()
														.addContainerGap()
														.add(
																layout
																		.createParallelGroup(
																				org.jdesktop.layout.GroupLayout.BASELINE)
																		.add(
																				jTextFieldInitialState,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																		.add(
																				jLabelInitialState)
																		.add(
																				jLabelGlobalResult))
														.addPreferredGap(
																org.jdesktop.layout.LayoutStyle.RELATED)
														.add(
																jPanel1,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																org.jdesktop.layout.LayoutStyle.RELATED)
														.add(
																jPanel2,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																org.jdesktop.layout.LayoutStyle.RELATED)
														.add(
																jPanel3,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.addContainerGap(42,
																Short.MAX_VALUE))));
	}// </editor-fold>

	// Variables declaration - do not modify
	private javax.swing.JButton jButtonExecuteSide1;
	private javax.swing.JButton jButtonExecuteSide2;
	private javax.swing.JButton jButtonExecuteSide3;
	private javax.swing.JButton jButtonExit;
	private javax.swing.JButton jButtonRun;
	private javax.swing.JLabel jLabelDelaySide1;
	private javax.swing.JLabel jLabelDelaySide2;
	private javax.swing.JLabel jLabelDelaySide3;
	private javax.swing.JLabel jLabelGlobalResult;
	private javax.swing.JLabel jLabelInitialState;
	private javax.swing.JLabel jLabelPositionSide1;
	private javax.swing.JLabel jLabelPositionSide2;
	private javax.swing.JLabel jLabelPositionSide3;
	private javax.swing.JLabel jLabelTextSide1;
	private javax.swing.JLabel jLabelTextSide2;
	private javax.swing.JLabel jLabelTextSide3;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JRadioButton jRadioButtonDelete;
	private javax.swing.JRadioButton jRadioButtonDeleteSide2;
	private javax.swing.JRadioButton jRadioButtonDeleteSide3;
	private javax.swing.JRadioButton jRadioButtonInsert;
	private javax.swing.JRadioButton jRadioButtonInsertSide2;
	private javax.swing.JRadioButton jRadioButtonInsertSide3;
	private javax.swing.JTextField jTextFieldDelay;
	private javax.swing.JTextField jTextFieldDelaySide2;
	private javax.swing.JTextField jTextFieldDelaySide3;
	private javax.swing.JTextField jTextFieldInitialState;
	private javax.swing.JTextField jTextFieldPosition;
	private javax.swing.JTextField jTextFieldPositionSide2;
	private javax.swing.JTextField jTextFieldPositionSide3;
	private javax.swing.JTextField jTextFieldResultSide1;
	private javax.swing.JTextField jTextFieldResultSide2;
	private javax.swing.JTextField jTextFieldResultSide3;
	private javax.swing.JTextField jTextFieldTextSide1;
	private javax.swing.JTextField jTextFieldTextSide2;
	private javax.swing.JTextField jTextFieldTextSide3;

	// End of variables declaration

	/**
	 * this method init additional listener and component settings.
	 */
	private void initListener() {
		/*set init document text. */
		jTextFieldInitialState.setText(resourceMap
				.getString("jTextFieldInitialState.text"));
		jTextFieldResultSide1.setText(resourceMap
				.getString("jTextFieldInitialState.text"));
		jTextFieldResultSide2.setText(resourceMap
				.getString("jTextFieldInitialState.text"));
		jTextFieldResultSide3.setText(resourceMap
				.getString("jTextFieldInitialState.text"));
		
		/* set action listener. */
		jButtonExecuteSide1.addActionListener(actionListener);
		jButtonExecuteSide2.addActionListener(actionListener);
		jButtonExecuteSide3.addActionListener(actionListener);
		
		jButtonExit.addActionListener(actionListener);
		jButtonRun.addActionListener(actionListener);
	}

	public class JupiterActionListener implements ActionListener {

		// Messages resourceMap;

		public void actionPerformed(ActionEvent event) {

			/* Exit program */
			if (resourceMap.getString("jButtonExit.text").equals(
					event.getActionCommand())) {
				System.exit(0);
			}
			/* run jupiter. :) */
			if (resourceMap.getString("jButtonRun.text").equals(
					event.getActionCommand())) {
				jupiter.simulate();
			}
			if (resourceMap.getString("jButtonExecuteSide1.text").equals(
					event.getActionCommand())) {
				jupiter.simulate(SIDE_1);
			}
			if (resourceMap.getString("jButtonExecuteSide2.text").equals(
					event.getActionCommand())) {
				jupiter.simulate(SIDE_2);
			}
			if (resourceMap.getString("jButtonExecuteSide3.text").equals(
					event.getActionCommand())) {
				jupiter.simulate(SIDE_3);
			}
		}
	}

	public class JupiterSimulater implements JupiterDocumentListener {

		protected SimulateNetzwork network;

		protected ClientSynchronizedDocument2 c1;
		protected ClientSynchronizedDocument2 c2;
		protected ClientSynchronizedDocument2 c3;
		protected ServerSynchronizedDocument2 s1;

		JID jid_c1;
		JID jid_c2;
		JID jid_c3;

		public JupiterSimulater() {
			init();
			// deactiveSide3();
		}

		public void deactiveSide3() {
			network.removeClient(c3);
			s1.removeProxyClient(jid_c3);
			jPanel3.setVisible(false);
			update();
		}

		private void init() {

			network = new SimulateNetzwork();

			jid_c1 = new JID("ori79@jabber.cc");
			jid_c2 = new JID("ori80@jabber.cc");
			jid_c3 = new JID("ori81@jabber.cc");
			JID jid_server = new JID("ori78@jabber.cc");

			c1 = new ClientSynchronizedDocument2(jTextFieldInitialState
					.getText(), network, jid_c1);
			c1.addJupiterDocumentListener(this);
			c2 = new ClientSynchronizedDocument2(jTextFieldInitialState
					.getText(), network, jid_c2);
			c2.addJupiterDocumentListener(this);
			c3 = new ClientSynchronizedDocument2(jTextFieldInitialState
					.getText(), network, jid_c3);
			c3.addJupiterDocumentListener(this);
			s1 = new ServerSynchronizedDocument2(jTextFieldInitialState
					.getText(), network, jid_server);

			network.addClient(c1);
			network.addClient(c2);
			network.addClient(c3);
			network.addClient(s1);

			/* create proxyqueues. */
			s1.addProxyClient(jid_c1);
			s1.addProxyClient(jid_c2);
			s1.addProxyClient(jid_c3);
		}

		public void simulate() {
			try {
				executeSide1();
				executeSide2();
				executeSide3();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), e.getMessage());
			}
		}

		public void simulate(String side) {
			try {
				if (side.endsWith(SIDE_1)) {
					executeSide1();
				}
				if (side.endsWith(SIDE_2)) {
					executeSide2();
				}
				if (side.endsWith(SIDE_3)) {
					executeSide3();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), "Error");
			}
		}

		private void executeSide1() throws Exception {
			Operation operation = null;
			try {
				/* get op type */
				if (jRadioButtonDelete.isSelected()) {
					operation = new DeleteOperation(Integer
							.parseInt(jTextFieldPosition.getText()),
							jTextFieldTextSide1.getText());
				}
				if (jRadioButtonInsert.isSelected()) {
					operation = new InsertOperation(Integer
							.parseInt(jTextFieldPosition.getText()),
							jTextFieldTextSide1.getText());
				}
			} catch (Exception e) {
				logger.error("Error in side 1 operation creation", e);
			}
			if (operation != null)
				c1.sendOperation(operation, Integer.parseInt(jTextFieldDelay
						.getText()));
		}

		private void executeSide2() throws Exception {
			Operation operation = null;
			try {
				/* get op type */
				if (jRadioButtonDeleteSide2.isSelected()) {
					operation = new DeleteOperation(Integer
							.parseInt(jTextFieldPositionSide2.getText()),
							jTextFieldTextSide2.getText());
				}
				if (jRadioButtonInsertSide2.isSelected()) {
					operation = new InsertOperation(Integer
							.parseInt(jTextFieldPositionSide2.getText()),
							jTextFieldTextSide2.getText());
				}

			} catch (Exception e) {
				logger.error("Error in side 2 operation creation", e);
			}
			if (operation != null)
				c2.sendOperation(operation, Integer
						.parseInt(jTextFieldDelaySide2.getText()));
		}

		private void executeSide3() throws Exception {
			Operation operation = null;
			try {
				/* get op type */
				if (jRadioButtonDeleteSide3.isSelected()) {
					operation = new DeleteOperation(Integer
							.parseInt(jTextFieldPositionSide3.getText()),
							jTextFieldTextSide3.getText());
				}
				if (jRadioButtonInsertSide2.isSelected()) {
					operation = new InsertOperation(Integer
							.parseInt(jTextFieldPositionSide3.getText()),
							jTextFieldTextSide3.getText());
				}
			} catch (Exception e) {
				logger.error("Error in side 3 operation creation", e);
			}
			if (operation != null)
				c3.sendOperation(operation, Integer
						.parseInt(jTextFieldDelaySide3.getText()));
		}

		/**
		 * update ui with new document state.
		 * 
		 * @param side
		 */
		public void documentAction(JID jid) {
			if (jid.equals(jid_c1)) {
				jTextFieldResultSide1.setText(c1.getDocument());
				update();
			}
			if (jid.equals(jid_c2)) {
				jTextFieldResultSide2.setText(c2.getDocument());
				update();
			}
			if (jid.equals(jid_c3)) {
				jTextFieldResultSide3.setText(c3.getDocument());
				update();
			}
		}

		public String getID() {
			return "TEST";

		}
	}
}
