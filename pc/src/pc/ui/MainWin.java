package pc.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pc.service.CustomerService;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

public class MainWin {

	private JFrame frame;
	private JTextField inputText;
	private JTextField outputText;
	private Properties p;
	private static final Logger logger = LoggerFactory.getLogger(MainWin.class);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWin window = new MainWin();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWin() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		File configFile = new File("config.properties");
		System.out.println(configFile.getAbsolutePath());
		logger.info("Load config.properties: " + configFile.getAbsolutePath());
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(configFile));
			p = new Properties();
			p.load(in);
		} catch (FileNotFoundException e2) {
			logger.error("config.properties load error......", e2);
			JOptionPane.showMessageDialog(null, "初始化文件丢失: " + configFile.getAbsolutePath(), "错误", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (IOException e1) {
			logger.error("config.properties load error......", e1);
			JOptionPane.showMessageDialog(null, "初始化读取错误", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		//init mutli-process
		String syn = p.getProperty("syn");
		String threadCount = p.getProperty("threadCount");
		String remoteServerUrl = p.getProperty("remoteServerUrl");
		String remoteUser = p.getProperty("remoteUser");
//		System.out.println(System.getProperty("user.dir"));
		frame = new JFrame();
		frame.setBounds(100, 100, 544, 322);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel label = new JLabel("\u7528\u6237\u540D");
		label.setBounds(37, 29, 46, 14);
		frame.getContentPane().add(label);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(83, 26, 118, 20);
		String userStr = p.getProperty("users");
		if (StringUtils.isBlank(userStr)) {
			JOptionPane.showMessageDialog(null, "缺少用户名", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String[] userNames = userStr.split(",");
		for (String userName : userNames) {
			comboBox.addItem(userName);
		}
		frame.getContentPane().add(comboBox);
		
		inputText = new JTextField();
		inputText.setBounds(83, 68, 268, 20);
		frame.getContentPane().add(inputText);
		inputText.setColumns(10);
		
		JLabel label_1 = new JLabel("\u8F93\u5165\u6587\u4EF6");
		label_1.setBounds(25, 71, 46, 14);
		frame.getContentPane().add(label_1);
		
		JLabel label_2 = new JLabel("\u8F93\u51FA\u8DEF\u5F84");
		label_2.setBounds(25, 109, 46, 14);
		frame.getContentPane().add(label_2);
		
		outputText = new JTextField();
		outputText.setText("C:\\output");
		outputText.setBounds(83, 106, 268, 20);
		frame.getContentPane().add(outputText);
		outputText.setColumns(10);
		
		JButton button_1 = new JButton("\u9009\u62E9");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
				jfc.showDialog(new JLabel(), "选择");
				File file = jfc.getSelectedFile();
				if (file != null) {
					logger.debug(file.getAbsolutePath());
					System.out.println(file.getAbsolutePath());
					System.out.println(jfc.getSelectedFile().getName());
					outputText.setText(file.getAbsolutePath());
				}
			}
		});
		button_1.setBounds(377, 101, 89, 23);
		frame.getContentPane().add(button_1);
		
		JButton button = new JButton("\u6D4F\u89C8");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
				jfc.showDialog(new JLabel(), "选择");
				File file = jfc.getSelectedFile();
				if (file != null) {
					logger.debug(file.getAbsolutePath());
					System.out.println(file.getAbsolutePath());
					System.out.println(jfc.getSelectedFile().getName());
					inputText.setText(file.getAbsolutePath());
				}
			}
		});
		button.setBounds(377, 67, 89, 23);
		frame.getContentPane().add(button);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setMaximum(200);
		progressBar.setBounds(48, 150, 411, 20);
		frame.getContentPane().add(progressBar);
		
		JButton btnNewButton = new JButton("\u83B7\u53D6\u6587\u4EF6");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = (String) comboBox.getSelectedItem();
				if (StringUtils.isBlank(user)) {
					JOptionPane.showMessageDialog(null, "请选择用户", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				File inputFile = new File(inputText.getText());
				if (!inputFile.exists()) {
					JOptionPane.showMessageDialog(null, "输入文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				File outputPath = new File(outputText.getText());
				if (!outputPath.isDirectory()) {
					JOptionPane.showMessageDialog(null, "输出路径错误", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (StringUtils.isBlank(user)) {
					JOptionPane.showMessageDialog(null, "缺少用户名", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String accessToken = p.getProperty(user+ ".accessToken");
				if (accessToken == null) {
					JOptionPane.showMessageDialog(null, "缺少用户" + user + "accessToken配置", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String pubArgs = p.getProperty(user + ".pubArgs");
				if (pubArgs == null) {
					JOptionPane.showMessageDialog(null, "缺少用户" + user + "pubArgs配置", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String password = p.getProperty(user + ".password");
				if (password == null) {
					JOptionPane.showMessageDialog(null, "缺少用户" + user + "password配置", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String userId = p.getProperty(user + ".userId");
				if (userId == null) {
					JOptionPane.showMessageDialog(null, "缺少用户" + user + "userId配置", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				CustomerService task = new CustomerService() {
					@Override
		            protected void done() {
		                String result = null;
		                try {
		                    result = get();
		                } catch (Exception e) {
		                	logger.error("CustomerService complete error......", e);
		                    e.printStackTrace();
		                }
		                progressBar.setValue(progressBar.getMaximum());
		                if (StringUtils.isNotBlank(syn)) {
							progressBar.setIndeterminate(false);
						}
		                JOptionPane.showMessageDialog(null, result);
		                btnNewButton.setEnabled(true);
		            }
					
					@Override
		            protected void process(List<Integer> chunks) {
		                Integer progressValue = chunks.get(0);
		                if (progressValue > progressBar.getMaximum()) {
		                	progressBar.setValue(progressBar.getMaximum());
		                } else {
		                	progressBar.setValue(progressValue);
		                }
		                
		            }
				};
				task.setInputFile(inputFile);
				task.setOutputPath(outputPath);
				task.setAccessToken(accessToken);
				task.setPassword(password);
				task.setPubArgs(pubArgs);
				task.setUserId(userId);
				task.setUserName(user);
				task.setSyn(syn);
				task.setCyclical(Integer.valueOf(threadCount));
				task.setRemoteUser(remoteUser);
				task.setRemoteServerUrl(remoteServerUrl);
//		        task.addPropertyChangeListener(new PropertyChangeListener() {
//		            public void propertyChange(PropertyChangeEvent evt) {
//		                if ("progress".equals(evt.getPropertyName())) {
//		                    Integer progressValue = (Integer) evt.getNewValue();
//		                    progressBar.setValue(progressValue);
//		                }
//		            }
//		        });
				progressBar.setValue(0);
				if (StringUtils.isNotBlank(syn)) {
					progressBar.setIndeterminate(true);
					progressBar.setStringPainted(false);
				}
				btnNewButton.setEnabled(false);
		        task.execute();
			}
		});
		btnNewButton.setBounds(183, 205, 89, 23);
		frame.getContentPane().add(btnNewButton);
		
		JCheckBox checkBox = new JCheckBox("\u5F39\u7A97");
		checkBox.setBounds(222, 25, 52, 23);
		frame.getContentPane().add(checkBox);
		
		JCheckBox checkBox_1 = new JCheckBox("\u8BE6\u7EC6");
		checkBox_1.setBounds(276, 25, 64, 23);
		frame.getContentPane().add(checkBox_1);
		
	}
}
