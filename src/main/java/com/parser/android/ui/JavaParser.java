package com.parser.android.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import com.parser.android.model.SimpleClassModel;
import com.parser.android.operations.FilesOperation;

public class JavaParser extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static int seconds = 0;
	static int minute = 0;
	static int activityCount = 1;
	static boolean state = false;
	public static String dirPath;

	public static JProgressBar progressBar;
	private JScrollPane scrollPane;
	private JPanel contentPane;
	private JLabel lblNewLabel;
	private static JPanel resultPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JavaParser frame = new JavaParser();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JavaParser() {
		setMinimumSize(new Dimension(800, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 559, 379);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JButton btnNewButton = new JButton("Click here to select project directory");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// create an object of JFileChooser class
				JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				j.addChoosableFileFilter(new FileNameExtensionFilter("Java file", "java"));
				// set the selection mode to directories only
				j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// invoke the showsOpenDialog function to show the save dialog
				int r = j.showOpenDialog(null);

				if (r == JFileChooser.APPROVE_OPTION) {
					resetUI();
					Timer t = new Timer(1000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							seconds++;
							if (seconds > 60) {
								minute++;
								seconds = 0;
							}
							lblNewLabel.setText(String.format("%02d", minute) + ":" + String.format("%02d", seconds));
						}
					});
					t.start();

					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								// get path of the selected directory
								dirPath = j.getSelectedFile().getAbsolutePath();
								File fileRoot = new File(j.getSelectedFile().getAbsolutePath());
								FilesOperation checker = new FilesOperation(fileRoot);
								//Extracting Activity and Fragment
								List<File> lst = checker.getActivityFiles();
								if (lst.size() > 0) {
									//Check resource used on Each activity
									List<SimpleClassModel> file = checker.getActivityDetails(lst);
									if (file.size() > 0) {
										//check resource passed or not
										checker.performResourceCheck(file);
									} else {
										// show in result file not found
										JLabel failedLbl = new JLabel("No android resource found.");
										failedLbl.setForeground(Color.RED);
										failedLbl.setFont(new Font("Arial", Font.BOLD, 12));
										resultPanel.add(failedLbl);
										resultPanel.validate();
										resultPanel.repaint();
									}
								} else {
									// show in result file not found
									JLabel failedLbl = new JLabel("No android file found.");
									failedLbl.setForeground(Color.RED);
									failedLbl.setFont(new Font("Arial", Font.BOLD, 12));
									resultPanel.add(failedLbl);
									resultPanel.validate();
									resultPanel.repaint();
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							} finally {
								t.stop();
								// HIDE PROGRESS BAR
								progressBar.setVisible(false);
								// SHOW RESULT
								scrollPane.setVisible(true);
								// Process completed display in panel
								JLabel recommendedTextLbl = new JLabel("Completed.");
								recommendedTextLbl.setFont(new Font("Arial", Font.BOLD, 12));
								resultPanel.add(recommendedTextLbl);
								resultPanel.validate();
								resultPanel.repaint();
							}
						}
					});

					thread.start();
				}
			}
		});

		lblNewLabel = new JLabel("00:00");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);

		scrollPane = new JScrollPane();
		scrollPane.setVisible(false);
		JLabel lblNewLabel_1 = new JLabel("Result");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane.setColumnHeaderView(lblNewLabel_1);
		resultPanel = new JPanel();
		resultPanel.setBackground(Color.WHITE);
		resultPanel.setForeground(new Color(0, 0, 0));
		scrollPane.setViewportView(resultPanel);
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING,
						gl_contentPane.createSequentialGroup().addGap(277)
								.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE).addGap(266))
				.addGroup(gl_contentPane.createSequentialGroup().addGap(5)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE).addGap(5))
				.addGroup(Alignment.TRAILING,
						gl_contentPane.createSequentialGroup().addGap(87)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 400,
												Short.MAX_VALUE)
										.addComponent(btnNewButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 400,
												Short.MAX_VALUE))
								.addGap(87)));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup().addGap(6)
				.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE).addGap(18)
				.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE).addGap(18)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE).addGap(6)));
		contentPane.setLayout(gl_contentPane);
	}

	/**
	 * @param fileName - Display result file status in panel green and red
	 * @param status - file check passed or not
	 * @param resource - resource name accessed in file
	 * @param recommendedText - recommended text if status failed
	 */
	public static void showResult(String fileName, boolean status, String resource, String recommendedText) {
		if (status) {
			// Show result in green check passed
			JLabel successLabel = new JLabel(activityCount + ". " + fileName + ": " + resource + " passed.");
			successLabel.setForeground(new Color(0, 255, 0));
			successLabel.setFont(new Font("Arial", Font.PLAIN, 12));
			resultPanel.add(successLabel);
		} else {
			// Show result in red check failed
			JLabel failedLbl = new JLabel(activityCount + ". " + fileName + ": " + resource + " Failed.");
			failedLbl.setForeground(Color.RED);
			failedLbl.setFont(new Font("Arial", Font.PLAIN, 12));
			resultPanel.add(failedLbl);
			JLabel recommendedTextLbl = new JLabel("**" + recommendedText);
			recommendedTextLbl.setFont(new Font("Arial", Font.ITALIC, 12));
			resultPanel.add(recommendedTextLbl);
		}
		// only to add blank space after each file status
		JLabel recommendedTextLbl = new JLabel(" ");
		recommendedTextLbl.setFont(new Font("Arial", Font.ITALIC, 11));
		resultPanel.add(recommendedTextLbl);
		resultPanel.validate();
		resultPanel.repaint();
		activityCount++;
	}

	// function to increase progress
	public static void fill(int completedFile, int totalFiles) {
		int i = 0;
		try {
			i = (completedFile * 100) / totalFiles;
			// fill the menu bar
			progressBar.setValue(i);
		} catch (Exception e) {
		}
	}

	void resetUI() {
		// reset progress bar
		progressBar.setVisible(true);
		progressBar.setValue(0);
		// HIDE result view
		scrollPane.setVisible(false);
		// reset result view
		resultPanel.removeAll();
		resultPanel.validate();
		resultPanel.repaint();
		// RESET TIMER
		minute = 0;
		seconds = 0;
		activityCount = 1;
	}

}
