package com.example.myapplication.Data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.cardview.widget.CardView;

public class ExpenseAdapter extends ArrayAdapter<Expense> {
    private Activity activity;
    private List<Expense> lExpense;
    private static LayoutInflater inflater = null;
    private HashMap<String , Integer> iconMapper = new HashMap<>();
    public ExpenseAdapter (Activity activity, int textViewResourceId,List<Expense> _lExpense) {
        super(activity, textViewResourceId);
        try {
            this.activity = activity;
            this.lExpense = _lExpense;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    public int getCount() {
        return lExpense.size();
    }

    public Expense getItem(Expense position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView display_amount;
        public ImageView icon;
        public TextView display_type;
        public TextView display_date;
        public CardView cardView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {

                iconMapper.put("food" , R.drawable.baseline_fastfood_24);
                iconMapper.put("bills",R.drawable.baseline_mood_bad_24);
                iconMapper.put("supplies",R.drawable.baseline_shopping_cart_24);
                iconMapper.put("fun" , R.drawable.baseline_whatshot_24);
                vi = inflater.inflate(R.layout.listview_item, null);
                holder = new ViewHolder();

                holder.display_amount = (TextView) vi.findViewById(R.id.tv_expenseamountindiv);
                holder.display_type = vi.findViewById(R.id.tv_expensetypeindiv);
                holder.display_date = vi.findViewById(R.id.tv_expensedateindiv);
                holder.icon = vi.findViewById(R.id.iv_expensetrackericon);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            System.out.println("amount in adapter: " + lExpense.get(position).amount);

            holder.display_amount.setText( "$"+Integer.toString(lExpense.get(position).amount));
            holder.display_type.setText(lExpense.get(position).type);
            String date = getDate(lExpense.get(position).currentDate);
            holder.display_date.setText(date);
            holder.icon.setImageResource(iconMapper.get(lExpense.get(position).type));
        } catch (Exception e) {


        }
        return vi;
    }
    public String getDate(long millis)
    {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = formatter.format(new Date(millis));
        return dateString;
    }
}