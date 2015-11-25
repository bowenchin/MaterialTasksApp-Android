package com.bowenchin.android.materialtasks.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bowenchin.android.materialtasks.R;
import com.bowenchin.android.materialtasks.model.Task;
import com.bowenchin.android.materialtasks.model.TaskLab;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import java.util.ArrayList;

/**
 * Created by bowenchin on 21/7/2015.
 */
public class TaskListFragment extends ListFragment {
    private ArrayList<Task> mTasks;
    private static final String TAG = "TaskListFragment";
    private Callbacks mCallbacks;
    private ImageView mColorImageView;
    private LinearLayout emptyView;

    /*Required interface for hosting activities*/
    public interface Callbacks{
        void onTaskSelected(Task task);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void updateUI(){
        ((TaskAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.tasks_title);
        mTasks= TaskLab.get(getActivity()).getTasks();
        TaskAdapter adapter = new TaskAdapter(mTasks);
        setListAdapter(adapter);

    }

    @Override
    public void onResume(){
        super.onResume();
        ((TaskAdapter)getListAdapter()).notifyDataSetChanged();
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        //Task t = (Task)(getListAdapter()).getItem(position);
        Task t = ((TaskAdapter)getListAdapter()).getItem(position);

        /*//Start TaskActivity
        Intent taskActivity = new Intent(getActivity(),TaskView.class);
        taskActivity.putExtra(TaskFragment.EXTRA_TASK_ID, t.getId());
        startActivity(taskActivity);*/

        mCallbacks.onTaskSelected(t);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((TaskAdapter)getListAdapter()).notifyDataSetChanged();

    }

    private class TaskAdapter extends ArrayAdapter<Task>{
        public TaskAdapter(ArrayList<Task> tasks){
            super(getActivity(),0,tasks);
            //super(getActivity(), android.R.layout.simple_list_item_1, tasks);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //if no view, inflate one
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_task,null);
            }

            // Configure the view for this Task
            Task t = getItem(position);

            TextView titleTextView = (TextView)convertView.findViewById(R.id.task_list_item_titleTextView);
            titleTextView.setText(t.getTitle());
            TextView subjectTextView = (TextView)convertView.findViewById(R.id.task_list_item_subjectTextView);
            subjectTextView.setText(t.getSubject());
            TextView dateTextView=(TextView)convertView.findViewById(R.id.task_list_item_dateTextView);
            dateTextView.setText(DateFormat.format("EEEE, MMM dd, yyyy", t.getDate()).toString());
            //CheckBox checkedCheckBox = (CheckBox)convertView.findViewById(R.id.task_list_item_solvedCheckBox);
            //checkedCheckBox.setChecked(t.isChecked());

            //ImageView mColorImageView = (ImageView)convertView.findViewById(R.id.toDoListItemColorImageView);
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getRandomColor();

            //int color = R.color.colorAccent;

            String title = t.getTitle();

            if(title==null)
            {
                //EditText is empty
                title="##";
            }
            else
            {
                //EditText is not empty
                title=t.getTitle();
            }

            TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                    .textColor(Color.WHITE)
                    .useFont(Typeface.DEFAULT)
                    .toUpperCase()
                    .endConfig()
                    .buildRound(title.substring(0, 1),color);

            mColorImageView = (ImageView)convertView.findViewById(R.id.toDoListItemColorImageView);
            mColorImageView.setImageDrawable(myDrawable);

            return convertView;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,View v,ContextMenu.ContextMenuInfo menuInfo){
        getActivity().getMenuInflater().inflate(R.menu.task_list_item_context,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        TaskAdapter adapter = (TaskAdapter)getListAdapter();
        Task task = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_task:
                TaskLab.get(getActivity()).deleteTask(task);
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup parent, Bundle savedInstanceState){
        /*View v = super.onCreateView(inflater, parent, savedInstanceState);
        ListView listView = (ListView)v.findViewById(android.R.id.list);
        listView.setEmptyView(v.findViewById(android.R.id.empty));*/

        View v = inflater.inflate(R.layout.fragment_empty_list, parent, false);
        ListView listView = (ListView)v.findViewById(android.R.id.list);
        listView.setEmptyView(v.findViewById(android.R.id.empty));

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_list_out);
        v.startAnimation(animation);

        final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(listView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                TaskAdapter adapter = (TaskAdapter) getListAdapter();
                                TaskLab taskLab = TaskLab.get(getActivity());
                                /*for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        taskLab.deleteTask(adapter.getItem(i));
                                    }
                                }*/
                                taskLab.deleteTask(adapter.getItem(position));
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {
                }
            }
        });

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            //User floating context menus
            registerForContextMenu(listView);
        }
        else{
            //Contextual action bar
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.task_list_item_context, menu);
                    return true;
                }

                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_task:
                            TaskAdapter adapter = (TaskAdapter) getListAdapter();
                            TaskLab taskLab = TaskLab.get(getActivity());
                            for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                if (getListView().isItemChecked(i)) {
                                    taskLab.deleteTask(adapter.getItem(i));
                                }
                            }
                            mode.finish();
                            adapter.notifyDataSetChanged();
                            return true;
                        default:
                            return false;
                    }
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {

                }
            });
        }
        return v;
    }

}
