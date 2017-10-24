package com.omarea.vboot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;

import com.omarea.shared.ConfigInfo;
import com.omarea.shared.EventBus;
import com.omarea.shared.Events;
import com.omarea.shared.ServiceHelper;
import com.omarea.shared.cmd_shellTools;
import com.omarea.shell.Platform;
import com.omarea.ui.list_adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.OnClickListener;
import static android.widget.AdapterView.GONE;
import static android.widget.AdapterView.OnItemClickListener;
import static android.widget.AdapterView.VISIBLE;


public class fragment_config extends Fragment {
    cmd_shellTools cmdshellTools = null;
    main thisview = null;

    public static Fragment Create(main thisView, cmd_shellTools cmdshellTools) {
        fragment_config fragment = new fragment_config();
        fragment.cmdshellTools = cmdshellTools;
        fragment.thisview = thisView;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_config, container, false);
    }

    Button btn_config_service_not_active;
    Button btn_config_dynamicservice_not_active;

    @Override
    public void onResume() {
        boolean serviceState = ServiceHelper.Companion.serviceIsRunning(getContext());
        btn_config_service_not_active.setVisibility(serviceState ? GONE : VISIBLE);
        btn_config_dynamicservice_not_active.setVisibility((serviceState && !ConfigInfo.getConfigInfo().DyamicCore) ? VISIBLE : GONE);

        super.onResume();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        btn_config_service_not_active = (Button) view.findViewById(R.id.btn_config_service_not_active);
        btn_config_dynamicservice_not_active = (Button) view.findViewById(R.id.btn_config_dynamicservice_not_active);

        btn_config_service_not_active.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_config_dynamicservice_not_active.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(thisview, accessibility_settings.class);
                startActivity(intent);
            }
        });

        final FloatingActionButton btn = (FloatingActionButton) view.findViewById(R.id.config_addtodefaultlist);
        final TabHost tabHost = (TabHost) view.findViewById(R.id.configlist_tabhost);

        tabHost.setup();

        tabHost.addTab(tabHost.newTabSpec("def_tab").setContent(R.id.configlist_tab0).setIndicator("均衡"));
        tabHost.addTab(tabHost.newTabSpec("game_tab").setContent(R.id.configlist_tab1).setIndicator("性能"));
        tabHost.addTab(tabHost.newTabSpec("power_tab").setContent(R.id.configlist_tab2).setIndicator("省电"));
        tabHost.addTab(tabHost.newTabSpec("fast_tab").setContent(R.id.configlist_tab3).setIndicator("极速"));
        tabHost.addTab(tabHost.newTabSpec("fast_tab").setContent(R.id.configlist_tab4).setIndicator("忽略"));
        tabHost.addTab(tabHost.newTabSpec("confg_tab").setContent(R.id.configlist_tab5).setIndicator("设置"));
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                btn.setVisibility((tabHost.getCurrentTab() > 4) ? GONE : VISIBLE);
            }
        });

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (tabHost.getCurrentTab()) {
                    case 0: {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(thisview);
                        String[] items = new String[]{"添加选中到 -> 性能模式", "添加选中到 -> 省电模式", "添加选中到 -> 极速模式", "添加选中到 -> 忽略列表"};
                        final Configs[] configses = new Configs[]{ Configs.Game, Configs.PowerSave, Configs.Fast, Configs.Ignored };
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list_adapter listadapter = (list_adapter) config_defaultlist.getAdapter();
                                AddToList(ConfigInfo.getConfigInfo().defaultList, listadapter.states, configses[which]);
                                SetList();
                            }
                        });
                        builder.setIcon(R.drawable.ic_menu_profile).setTitle("设置配置模式").create().show();
                        break;
                    }
                    case 1: {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(thisview);
                        String[] items = new String[]{"添加选中到 -> 均衡模式", "添加选中到 -> 省电模式", "添加选中到 -> 极速模式", "添加选中到 -> 忽略列表"};
                        final Configs[] configses = new Configs[]{ Configs.Default, Configs.PowerSave, Configs.Fast, Configs.Ignored };
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list_adapter listadapter = (list_adapter) config_gamelist.getAdapter();
                                AddToList(ConfigInfo.getConfigInfo().gameList, listadapter.states, configses[which]);
                                SetList();
                            }
                        });
                        builder.setIcon(R.drawable.ic_menu_profile).setTitle("设置配置模式").create().show();
                        break;
                    }
                    case 2: {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(thisview);
                        String[] items = new String[]{"添加选中到 -> 均衡模式", "添加选中到 -> 性能模式", "添加选中到 -> 极速模式", "添加选中到 -> 忽略列表"};
                        final Configs[] configses = new Configs[]{ Configs.Default, Configs.Game, Configs.Fast, Configs.Ignored };
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list_adapter listadapter = (list_adapter) config_powersavelist.getAdapter();
                                AddToList(ConfigInfo.getConfigInfo().powersaveList, listadapter.states, configses[which]);
                                SetList();
                            }
                        });
                        builder.setIcon(R.drawable.ic_menu_profile).setTitle("设置配置模式").create().show();
                        break;
                    }
                    case 3: {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(thisview);
                        String[] items = new String[]{"添加选中到 -> 均衡模式", "添加选中到 -> 性能模式", "添加选中到 -> 省电模式", "添加选中到 -> 忽略列表"};
                        final Configs[] configses = new Configs[]{ Configs.Default, Configs.Game, Configs.PowerSave, Configs.Ignored };
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list_adapter listadapter = (list_adapter) config_fastlist.getAdapter();
                                AddToList(ConfigInfo.getConfigInfo().fastList, listadapter.states, configses[which]);
                                SetList();
                            }
                        });
                        builder.setIcon(R.drawable.ic_menu_profile).setTitle("设置配置模式").create().show();
                        break;
                    }
                    case 4: {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(thisview);
                        String[] items = new String[]{"添加选中到 -> 均衡模式", "添加选中到 -> 性能模式", "添加选中到 -> 省电模式", "添加选中到 -> 极速模式"};
                        final Configs[] configses = new Configs[]{ Configs.Default, Configs.Game, Configs.PowerSave, Configs.Fast };
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list_adapter listadapter = (list_adapter) config_ignoredlist.getAdapter();
                                AddToList(ConfigInfo.getConfigInfo().ignoredList, listadapter.states, configses[which]);
                                SetList();
                            }
                        });
                        builder.setIcon(R.drawable.ic_menu_profile).setTitle("设置配置模式").create().show();
                        break;
                    }
                }

            }
        });

        final Switch default_config_bigcore = (Switch) view.findViewById(R.id.default_config_bigcore);
        boolean useBigCore = ConfigInfo.getConfigInfo().UseBigCore;
        default_config_bigcore.setChecked(useBigCore);

        setConfigInfoText();

        default_config_bigcore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean useBigCore = default_config_bigcore.isChecked();
                ConfigInfo.getConfigInfo().UseBigCore = useBigCore;
                setConfigInfoText();
                EventBus.INSTANCE.publish(Events.INSTANCE.getCoreConfigChanged());
            }
        });

        final Switch config_showSystemApp = (Switch) view.findViewById(R.id.config_showSystemApp);
        config_showSystemApp.setChecked(ConfigInfo.getConfigInfo().HasSystemApp);
        config_showSystemApp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            ConfigInfo.getConfigInfo().HasSystemApp = config_showSystemApp.isChecked();
            LoadList();
            SetList();
            }
        });

        SetList();

        config_defaultlist = (ListView) view.findViewById(R.id.config_defaultlist);
        config_gamelist = (ListView) view.findViewById(R.id.config_gamelist);
        config_powersavelist = (ListView) view.findViewById(R.id.config_powersavelist);
        config_fastlist = (ListView) view.findViewById(R.id.config_fastlist);
        config_ignoredlist = (ListView) view.findViewById(R.id.config_ignoredlist);

        config_defaultlist.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View current, final int position, long id) {
                CheckBox select_state = (CheckBox) current.findViewById(R.id.select_state);
                if (select_state != null)
                    select_state.setChecked(!select_state.isChecked());
                ConfigInfo.getConfigInfo().defaultList.get(position).put("select_state", !select_state.isChecked());
            }
        });

        config_gamelist.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View current, final int position, long id) {
                CheckBox select_state = (CheckBox) current.findViewById(R.id.select_state);
                if (select_state != null)
                    select_state.setChecked(!select_state.isChecked());
                ConfigInfo.getConfigInfo().gameList.get(position).put("select_state", !select_state.isChecked());
            }
        });

        config_powersavelist.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View current, final int position, long id) {
                CheckBox select_state = (CheckBox) current.findViewById(R.id.select_state);
                if (select_state != null)
                    select_state.setChecked(!select_state.isChecked());
                ConfigInfo.getConfigInfo().powersaveList.get(position).put("select_state", !select_state.isChecked());
            }
        });

        config_fastlist.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View current, final int position, long id) {
                CheckBox select_state = (CheckBox) current.findViewById(R.id.select_state);
                if (select_state != null)
                    select_state.setChecked(!select_state.isChecked());
                ConfigInfo.getConfigInfo().fastList.get(position).put("select_state", !select_state.isChecked());
            }
        });

        config_ignoredlist.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View current, final int position, long id) {
                CheckBox select_state = (CheckBox) current.findViewById(R.id.select_state);
                if (select_state != null)
                    select_state.setChecked(!select_state.isChecked());
                ConfigInfo.getConfigInfo().ignoredList.get(position).put("select_state", !select_state.isChecked());
            }
        });
    }

    //设置配置文件提示信息
    private void setConfigInfoText() {
        final String cpuName = new Platform().GetCPUName();
        final TextView defaultconfighelp = (TextView) thisview.findViewById(R.id.defaultconfighelp);
        switch (cpuName.toLowerCase()) {
            case "msm8996": {
                defaultconfighelp.setText(ConfigInfo.getConfigInfo().UseBigCore ? R.string.defaultconfighelp_bigcore_820 : R.string.defaultconfighelp_820);
                break;
            }
            case "msm8998": {
                defaultconfighelp.setText(ConfigInfo.getConfigInfo().UseBigCore ? R.string.defaultconfighelp_bigcore_835 : R.string.defaultconfighelp_835);
                break;
            }
        }
    }

    ListView config_defaultlist;
    ListView config_gamelist;
    ListView config_powersavelist;
    ListView config_ignoredlist;
    ListView config_fastlist;

    final Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    void SetList() {
        thisview.progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (installedList == null)
                    LoadList();
                SetListData(ConfigInfo.getConfigInfo().defaultList, config_defaultlist);
                SetListData(ConfigInfo.getConfigInfo().gameList, config_gamelist);
                SetListData(ConfigInfo.getConfigInfo().powersaveList, config_powersavelist);
                SetListData(ConfigInfo.getConfigInfo().fastList, config_fastlist);
                SetListData(ConfigInfo.getConfigInfo().ignoredList, config_ignoredlist);
            }
        }).start();
    }

    void SetListData(final ArrayList<HashMap<String, Object>> dl, final ListView lv) {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                lv.setAdapter(new list_adapter(getContext(), dl));
                thisview.progressBar.setVisibility(View.GONE);
            }
        });
    }

    ArrayList<HashMap<String, Object>> installedList;

    ArrayList<HashMap<String, Object>> GetAppIcon(List<HashMap<String, Object>> list) {
        ArrayList<HashMap<String, Object>> newDic = new ArrayList<>();

        String pkg;
        for (HashMap<String, Object> row : list) {
            HashMap<String, Object> newRow = null;
            pkg = row.get("packageName").toString();

            for (HashMap<String, Object> item : installedList) {
                String pkg2 = item.get("packageName").toString();
                if (pkg.equals(pkg2)) {
                    newRow = new HashMap<String, Object>();
                    for (String key : item.keySet()) {
                        newRow.put(key, item.get(key));
                    }
                    break;
                }
            }
            if (newRow == null) {
                newRow = new HashMap<String, Object>();
                for (String key : row.keySet()) {
                    newRow.put(key, row.get(key));
                }
            }
            newDic.add(newRow);
        }
        return newDic;
    }

    void LoadList() {
        PackageManager packageManager = thisview.getPackageManager();
        List<ApplicationInfo> packageInfos = packageManager.getInstalledApplications(0);

        installedList = new ArrayList<HashMap<String, Object>>();/*在数组中存放数据*/
        Boolean hasSystemApp = ConfigInfo.getConfigInfo().HasSystemApp;
        for (int i = 0; i < packageInfos.size(); i++) {
            if (!hasSystemApp && (packageInfos.get(i).sourceDir.indexOf("/system") == 0)) {
                continue;
            }

            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("icon", packageInfos.get(i).loadIcon(packageManager));
            item.put("name", packageInfos.get(i).loadLabel(packageManager));
            item.put("packageName", packageInfos.get(i).packageName.toLowerCase());
            item.put("select_state", false);
            installedList.add(item);
        }
        ConfigInfo.getConfigInfo().gameList = GetAppIcon(ConfigInfo.getConfigInfo().gameList);
        ConfigInfo.getConfigInfo().powersaveList = GetAppIcon(ConfigInfo.getConfigInfo().powersaveList);
        ConfigInfo.getConfigInfo().fastList = GetAppIcon(ConfigInfo.getConfigInfo().fastList);
        ConfigInfo.getConfigInfo().ignoredList = GetAppIcon(ConfigInfo.getConfigInfo().ignoredList);
        ConfigInfo.getConfigInfo().defaultList = (ArrayList<HashMap<String, Object>>) installedList.clone();

        for (HashMap<String, Object> item : ConfigInfo.getConfigInfo().gameList) {
            RemoveItem(ConfigInfo.getConfigInfo().defaultList, item);
        }
        for (HashMap<String, Object> item : ConfigInfo.getConfigInfo().powersaveList) {
            RemoveItem(ConfigInfo.getConfigInfo().defaultList, item);
        }
        for (HashMap<String, Object> item : ConfigInfo.getConfigInfo().fastList) {
            RemoveItem(ConfigInfo.getConfigInfo().defaultList, item);
        }
        for (HashMap<String, Object> item : ConfigInfo.getConfigInfo().ignoredList) {
            RemoveItem(ConfigInfo.getConfigInfo().defaultList, item);
        }
    }

    /**
     * 从当前列表中获取已选中的应用，添加到指定模式
     * @param list 当前列表
     * @param postions 各个序号的选中状态
     * @param config 指定的新模式
     */
    void AddToList(ArrayList<HashMap<String, Object>> list, HashMap<Integer, Boolean> postions, Configs config) {
        ArrayList<HashMap<String, Object>> selectedItems = new ArrayList<>();
        for (int position : postions.keySet()) {
            if (postions.get(position) == true) {
                selectedItems.add(list.get(position));
            }
        }
        switch (config) {
            case Default: {
                for (HashMap<String, Object> item : selectedItems) {
                    ConfigInfo.getConfigInfo().gameList.remove(item);
                    ConfigInfo.getConfigInfo().powersaveList.remove(item);
                    ConfigInfo.getConfigInfo().fastList.remove(item);
                    ConfigInfo.getConfigInfo().ignoredList.remove(item);
                }
                break;
            }
            case Game: {
                for (HashMap<String, Object> item : selectedItems) {
                    ConfigInfo.getConfigInfo().powersaveList.remove(item);
                    ConfigInfo.getConfigInfo().fastList.remove(item);
                    ConfigInfo.getConfigInfo().ignoredList.remove(item);
                    ConfigInfo.getConfigInfo().gameList.add(item);
                }
                break;
            }
            case PowerSave: {
                for (HashMap<String, Object> item : selectedItems) {
                    ConfigInfo.getConfigInfo().gameList.remove(item);
                    ConfigInfo.getConfigInfo().fastList.remove(item);
                    ConfigInfo.getConfigInfo().ignoredList.remove(item);
                    ConfigInfo.getConfigInfo().powersaveList.add(item);
                }
                break;
            }
            case Fast: {
                for (HashMap<String, Object> item : selectedItems) {
                    ConfigInfo.getConfigInfo().gameList.remove(item);
                    ConfigInfo.getConfigInfo().powersaveList.remove(item);
                    ConfigInfo.getConfigInfo().ignoredList.remove(item);
                    ConfigInfo.getConfigInfo().fastList.add(item);
                }
                break;
            }
            case Ignored: {
                for (HashMap<String, Object> item : selectedItems) {
                    ConfigInfo.getConfigInfo().gameList.remove(item);
                    ConfigInfo.getConfigInfo().powersaveList.remove(item);
                    ConfigInfo.getConfigInfo().fastList.remove(item);
                    ConfigInfo.getConfigInfo().ignoredList.add(item);
                }
                break;
            }
        }
        try {
            LoadList();
        } catch (Exception ex) {

        }
    }

    void RemoveItem(List<HashMap<String, Object>> list, HashMap<String, Object> app) {
        String pkgName = app.get("packageName").toString();
        HashMap<String, Object> item = null;
        for (HashMap<String, Object> row : list) {
            if (row.get("packageName") != null && row.get("packageName").toString().equals(pkgName)) {
                item = row;
            }
        }
        if (item != null)
            list.remove(item);
    }

    //销毁：释放内存资源
    @Override
    public void onDestroy() {
        installedList.clear();

        SetListData(installedList, config_defaultlist);
        SetListData(installedList, config_gamelist);
        SetListData(installedList, config_powersavelist);

        for (int i = 0; i < ConfigInfo.getConfigInfo().defaultList.size(); i++) {
            ConfigInfo.getConfigInfo().defaultList.get(i).remove("icon");
        }
        for (int i = 0; i < ConfigInfo.getConfigInfo().gameList.size(); i++) {
            ConfigInfo.getConfigInfo().gameList.get(i).remove("icon");
        }
        for (int i = 0; i < ConfigInfo.getConfigInfo().powersaveList.size(); i++) {
            ConfigInfo.getConfigInfo().powersaveList.get(i).remove("icon");
        }
        for (int i = 0; i < ConfigInfo.getConfigInfo().fastList.size(); i++) {
            ConfigInfo.getConfigInfo().fastList.get(i).remove("icon");
        }
        for (int i = 0; i < ConfigInfo.getConfigInfo().ignoredList.size(); i++) {
            ConfigInfo.getConfigInfo().ignoredList.get(i).remove("icon");
        }

        super.onDestroy();
    }

    enum Configs {
        Default,
        Game,
        PowerSave,
        Fast,
        Ignored;
    }
}
