package jlx.tools.research.frame;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import jlx.tools.research.ZPWebConnInfo;
import jlx.tools.research.pop.AlertMgr;
import jlx.tools.research.procesor.ZPWebPageProcessor;
import jlx.tools.research.vo.CompanyInfo;
import jlx.tools.research.zhaopin.CorSearcher;
import jlx.tools.webfetcher.IUpdateTextFrame;
import jlx.tools.webfetcher.task.HttpTask;
import jlx.tools.webfetcher.task.TaskManager;
import jlx.util.ConfigUtil;
import jlx.util.log.DebugLogger;
import jlx.util.log.SystemOutSetter;
import jlx.util.string.FormatUtil;

/**
 * {class description} <br>
 * <p>
 * Create on : 2012-6-1<br>
 * <p>
 * </p>
 * <br>
 * 
 * @version CompanyResearchTool v1.0
 *          <p>
 *          <br>
 *          <strong>Modify History:</strong><br>
 *          user modify_date modify_content<br>
 *          -------------------------------------------<br>
 *          <br>
 */
public class TjxzxkMainFrame extends JFrame implements IUpdateTextFrame<CompanyInfo>,ItemListener {

    private final JPanel m_contentPane;
    /**
     * <code>collection</code> - {当前已经显示的公司信息}.
     */
    Collection<CompanyInfo> all = null;
    private final Object lock = new Object();
    private final TaskManager<CompanyInfo> taskManager = new TaskManager<CompanyInfo>(this);
    private final JButton m_button_start;
    private final JButton m_button_stop;
    private final JTextArea m_textArea;
    private final JScrollPane m_scrollPane;
    private final JPanel m_panel_chkboxs;
    private final JCheckBox m_checkBox_check;
    private final OwnFrame ownFrame = new OwnFrame();
    private final BossKeyHandler bossKeyHandler = new BossKeyHandler();
    private final JCheckBox chkShowPop = new JCheckBox("新公司弹框提示");

    /**
     * Launch the application.
     */
    public static void main(final String[] args) {
        SystemOutSetter.setSystOut("tjxzxk.log");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    TjxzxkMainFrame frame = new TjxzxkMainFrame();
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
    public TjxzxkMainFrame() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(TjxzxkMainFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/file.gif")));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(final WindowEvent e) {
                // 做一些初始化动作
                onWindowOpened();
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                onResize();
            }
        });
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 989, 602);
        m_contentPane = new JPanel();
        m_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(m_contentPane);
        m_contentPane.setLayout(null);

        m_button_stop = new JButton("停止");
        m_button_stop.setBounds(857, 10, 80, 23);
        m_contentPane.add(m_button_stop);
        m_button_stop.setEnabled(false);
        m_button_stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                stopFresh();
            }

        });
        m_button_stop.setFont(new Font("宋体", Font.PLAIN, 12));

        m_button_start = new JButton("开始自动刷新");
        m_button_start.setBounds(731, 10, 116, 23);
        m_contentPane.add(m_button_start);
        m_button_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                startFresh();

            }
        });
        m_button_start.setFont(new Font("宋体", Font.PLAIN, 12));

        m_scrollPane = new JScrollPane();
        m_scrollPane.setBounds(10, 67, 953, 492);
        m_contentPane.add(m_scrollPane);

        m_textArea = new JTextArea();
        m_scrollPane.setViewportView(m_textArea);

        m_panel_chkboxs = new JPanel();
        m_panel_chkboxs.setBounds(10, 10, 592, 58);
        m_contentPane.add(m_panel_chkboxs);
        m_panel_chkboxs.setLayout(null);

        JCheckBox checkBox = new JCheckBox("全市");
        checkBox.setBounds(0, 0, 49, 23);
        m_panel_chkboxs.add(checkBox);
        checkBox.setName("tjxzxk");
        checkBox.setFont(new Font("宋体", Font.PLAIN, 12));
        checkBox.addItemListener(this);
        checkBox.setSelected(true);

        JCheckBox checkBox_2 = new JCheckBox("保税区");
        checkBox_2.setName("tjxzxk.31");
        checkBox_2.addItemListener(this);
        checkBox_2.setSelected(true);
        checkBox_2.setBounds(50, 0, 62, 23);
        m_panel_chkboxs.add(checkBox_2);
        checkBox_2.setFont(new Font("宋体", Font.PLAIN, 12));

        JCheckBox checkBox_1 = new JCheckBox("高新区");
        checkBox_1.setName("tjxzxk.32");
        checkBox_1.addItemListener(this);
        checkBox_1.setSelected(true);
        checkBox_1.setBounds(114, 0, 61, 23);
        m_panel_chkboxs.add(checkBox_1);
        checkBox_1.setFont(new Font("宋体", Font.PLAIN, 12));

        JCheckBox chckbxTjxzxk = new JCheckBox("????");
        chckbxTjxzxk.setVisible(false);
        chckbxTjxzxk.setToolTipText("天津市行政审批服务网");
        chckbxTjxzxk.setName("???");
        chckbxTjxzxk.addItemListener(this);
        chckbxTjxzxk.setSelected(true);
        chckbxTjxzxk.setBounds(177, 0, 62, 23);
        m_panel_chkboxs.add(chckbxTjxzxk);
        chckbxTjxzxk.setFont(new Font("宋体", Font.PLAIN, 12));

        JCheckBox checkBox_4 = new JCheckBox("赶集");
        checkBox_4.setVisible(false);
        checkBox_4.setName("ganji");
        checkBox_4.addItemListener(this);
        checkBox_4.setBounds(240, 0, 49, 23);
        m_panel_chkboxs.add(checkBox_4);
        checkBox_4.setFont(new Font("宋体", Font.PLAIN, 12));

        JCheckBox checkBox_5 = new JCheckBox("58");
        checkBox_5.setVisible(false);
        checkBox_5.setName("58tc");
        checkBox_5.addItemListener(this);
        checkBox_5.setBounds(291, 0, 37, 23);
        m_panel_chkboxs.add(checkBox_5);
        checkBox_5.setFont(new Font("宋体", Font.PLAIN, 12));

        JCheckBox chckbxhr = new JCheckBox("01hr");
        chckbxhr.setVisible(false);
        chckbxhr.setToolTipText("数字英才");
        chckbxhr.setBounds(330, 0, 49, 23);
        m_panel_chkboxs.add(chckbxhr);
        chckbxhr.setName("01hr");
        chckbxhr.addItemListener(this);
        chckbxhr.setFont(new Font("宋体", Font.PLAIN, 12));
        
        JCheckBox chkJob1001 = new JCheckBox("job1001");
        chkJob1001.setVisible(false);
        chkJob1001.setToolTipText("一览英才网");
        chkJob1001.setName("job1001");
        chkJob1001.setFont(new Font("宋体", Font.PLAIN, 12));
        chkJob1001.setBounds(381, 0, 72, 23);
        chkJob1001.addItemListener(this);
        m_panel_chkboxs.add(chkJob1001);
        
        JCheckBox chkJmrc = new JCheckBox("jmrc");
        chkJmrc.setVisible(false);
        chkJmrc.setToolTipText("津门人才");
        chkJmrc.setName("jmrc");
        chkJmrc.addItemListener(this);
        chkJmrc.setFont(new Font("宋体", Font.PLAIN, 12));
        chkJmrc.setBounds(453, 0, 49, 23);
        m_panel_chkboxs.add(chkJmrc);
        
        JCheckBox chckbxRem = new JCheckBox("rem");
        chckbxRem.setVisible(false);
        chckbxRem.setToolTipText("新取得资质公司列表");
        chckbxRem.setName("rem");
        chckbxRem.addItemListener(this);
        chckbxRem.setFont(new Font("宋体", Font.PLAIN, 12));
        chckbxRem.setBounds(504, 0, 49, 23);
        m_panel_chkboxs.add(chckbxRem);

        JCheckBox checkBox_all = new JCheckBox("全选");
        checkBox_all.setFont(new Font("宋体", Font.PLAIN, 12));
        checkBox_all.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                Object source = e.getSource();
                if(source instanceof JCheckBox){
                    JCheckBox box = (JCheckBox) source;
                    chkboxAllSelected(box.isSelected());
                }
            }
        });
        checkBox_all.setBounds(608, 10, 49, 23);
        m_contentPane.add(checkBox_all);
        
        m_checkBox_check = new JCheckBox("不验证");
        m_checkBox_check.setFont(new Font("宋体", Font.PLAIN, 12));
        m_checkBox_check.setBounds(659, 10, 66, 23);
        m_contentPane.add(m_checkBox_check);
        chkShowPop.setSelected(true);
        chkShowPop.setBounds(608, 39, 173, 23);
        m_contentPane.add(chkShowPop);
        chkShowPop.setFont(new Font("宋体", Font.PLAIN, 12));

    }

    private void stopFresh() {
        taskManager.stop();
        
        m_button_stop.setEnabled(false);
        m_button_start.setEnabled(true);
    }

    /**
     * {method description}.
     */
    protected void onWindowOpened() {
        // ######################################新加的时候修改这个地方##########################################
        // 设置URL默认值
        /**
         * m_textField_51.setText(ConfigUtil.getDefaultURLByKey("51job"));
         * m_textField_taida.setText(ConfigUtil.getDefaultURLByKey("taida"));
         * m_textField_yicai.setText(ConfigUtil.getDefaultURLByKey("yicai"));
         * m_textField_chinahr.setText(ConfigUtil.getDefaultURLByKey("chinahr"));
         * m_textField_ganji.setText(ConfigUtil.getDefaultURLByKey("ganji"));
         */
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<新加的时候修改这个地方<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    }

    /**
     * {开始刷新}.
     */
    public void startFresh() {
        // ###########################################新加的时候修改这个地方##########################################
        // 加入任务列表
        /*
         * taskManager.addTask(new TaskInfo("51job",m_textField_51.getText())); taskManager.addTask(new
         * TaskInfo("taida",m_textField_taida.getText())); taskManager.addTask(new
         * TaskInfo("yicai",m_textField_yicai.getText())); taskManager.addTask(new
         * TaskInfo("chinahr",m_textField_chinahr.getText())); taskManager.addTask(new
         * TaskInfo("ganji",m_textField_ganji.getText()));
         */
        // taskManager.addTask(new TaskInfo("51job",ConfigUtil.getDefaultURLByKey("51job")));
        // taskManager.addTask(new TaskInfo("taida",ConfigUtil.getDefaultURLByKey("taida")));
        // taskManager.addTask(new TaskInfo("yicai",ConfigUtil.getDefaultURLByKey("yicai")));
        // taskManager.addTask(new TaskInfo("chinahr",ConfigUtil.getDefaultURLByKey("chinahr")));
        // //taskManager.addTask(new TaskInfo("58tc",ConfigUtil.getDefaultURLByKey("58tc")));
        // taskManager.addTask(new TaskInfo("01hr",ConfigUtil.getDefaultURLByKey("01hr")));

        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<新加的时候修改这个地方<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        m_button_stop.setEnabled(true);
        m_button_start.setEnabled(false);
        this.invalidate();

        taskManager.start();

    }

    /**
     * {method description}.
     */
    private void chkboxEnabled(final boolean isEnabled) {
        Component[] cs = m_panel_chkboxs.getComponents();
        for (int i = 0; i < cs.length; i++) {
            JCheckBox c = (JCheckBox)cs[i];
            c.setEnabled(isEnabled);
        }
    }
    private void chkboxAllSelected(final boolean selected) {
        Component[] cs = m_panel_chkboxs.getComponents();
        for (int i = 0; i < cs.length; i++) {
            JCheckBox c = (JCheckBox)cs[i];
            c.setSelected(selected);
        }
        
    }
    /**
     * {method description}.
     * 
     * @param web
     */
    private void addTask(final String web) {
        taskManager.addTask(newTask(web));
        //判断是否加入首页的逻辑
        String homeKey = web + ".home";
        if (ConfigUtil.getDefaultURLByKey(homeKey) != null) {
            taskManager.addTask(newTask(homeKey));
        }
    }
    //组装一个task
    private HttpTask<CompanyInfo> newTask(final String webKey){
        ZPWebConnInfo connInfo = new ZPWebConnInfo();
        connInfo.setWebKey(webKey);
        connInfo.setUrl(ConfigUtil.getDefaultURLByKey(webKey));
        return new HttpTask<CompanyInfo>(connInfo, new ZPWebPageProcessor());
    }
    /**
     * {method description}.
     * @param name
     */
    private void removeTask(final String web) {
        taskManager.removeTask(newTask(web));
        // 判断是否去掉首页的逻辑
        String homeKey = web + ".home";
        if (ConfigUtil.getDefaultURLByKey(homeKey) != null) {
            taskManager.removeTask(newTask(homeKey));
        }
    }

    /**
     * {更新內容 }.
     * 
     * @param collection
     */
    @Override
    public void updateAreaTxt(final List<CompanyInfo> collection) {
        synchronized (lock) {
            List<CompanyInfo> needAdd = new ArrayList<CompanyInfo>();

            all2List(collection, needAdd);
            //写到ownFrame我的列表里面
            ownFrame.updateAreaTxt(needAdd);
            // 没有归属的公司pop提示框
            if(chkShowPop.isSelected()){
                popNoneOwner(needAdd);
            }
            // 写数据
            StringBuffer buf = new StringBuffer();
            for (Iterator iterator = needAdd.iterator(); iterator.hasNext();) {
                CompanyInfo companyInfo = (CompanyInfo) iterator.next();
                String name = FormatUtil.addBlank(companyInfo.getName());
                buf.append("\n").append(name).append("\t").append(companyInfo.getOwnerDep())
                        .append("\t").append(companyInfo.getOwnerUser()).append("\t")
                        .append(companyInfo.getDetailURL());
            }
            m_textArea.append(buf.toString());
            // m_textArea.invalidate();
        }
    }
    // 过滤没有归属的新公司提示出来
    public void popNoneOwner(final List<CompanyInfo> list){
        StringBuffer buffer = new StringBuffer();
        int no=1;
        for (int i = 0; i < list.size(); i++) {
            CompanyInfo companyInfo = list.get(i);
            String owner = companyInfo.getOwnerUser();
            //归属人是空
            if(owner == null){
                buffer.append(no++).append(".").append(companyInfo.getName());
                //不超过4家公司
//                if(no>10){
//                    buffer.append("等\n");
//                    break;
//                }else{
                    buffer.append("\n");
//                }
            }
           
        }
        if(buffer.length()>0){
            AlertMgr.pop(buffer.toString());
        }
    }
   

    /**
     * 排除重复
     * 
     * @param collection
     * @param needAdd
     */
    private void all2List(final Collection<CompanyInfo> collection, List<CompanyInfo> needAdd) {
        if (all == null) {
            all = new ArrayList<CompanyInfo>();
        }
        for (Iterator<CompanyInfo> iterator = collection.iterator(); iterator.hasNext();) {
            CompanyInfo companyInfo = iterator.next();
            if (!all.contains(companyInfo)) {
                needAdd.add(companyInfo);
                all.add(companyInfo);
            }
        }
        // 如果更新30个，只放all，不加入列表显示，验证归属太慢
        if (needAdd.size() > 30) {
            DebugLogger.log("needAdd  大于 30 ,只保留前30！");
            needAdd = needAdd.subList(0, 29);
//            needAdd.clear();
        }
        // 过滤已经有人的
        // 先检查是否启用
        if (!m_checkBox_check.isSelected() && needAdd.size() < 30) {
            CorSearcher.search(needAdd);
        }

    }

    private void onResize() {
        m_textArea.setSize(this.getWidth() - 35, this.getHeight() - 113);
        m_scrollPane.setSize(this.getWidth() - 35, this.getHeight() - 113);
    }

    /*
     * (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
    public void itemStateChanged(final ItemEvent e) {
        Object source = e.getSource();
        if(source instanceof JCheckBox){
            JCheckBox box = (JCheckBox) source;
            if(box.isSelected()){
                addTask(box.getName());
            }else{
                removeTask(box.getName());
            }
        }
    }
    


    /**
     *  老板键操作，隐藏窗口
     */
    public void bossKeyAction() {
        bossKeyHandler.handle(this);
    }
    class BossKeyHandler{
        /**
         * <code>ownWindowVisble</code> - {记录我的列表窗口的visible状态，再次点击快捷键的时候恢复使用}.
         */
        private boolean ownWindowVisible;
        
        public void handle(final TjxzxkMainFrame mainFrame){
            if(mainFrame.isVisible()){
                ownWindowVisible = ownFrame.isVisible();
                mainFrame.setVisible(false);
                ownFrame.setVisible(false);
            }else{
                mainFrame.setVisible(true);
                ownFrame.setVisible(ownWindowVisible);
            }
            
        }
    }
}
