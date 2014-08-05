package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.text.NumberFormat;
import java.util.List;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;

public class CashFlowListAdapter extends ArrayAdapter<CashFlow> {

    public CashFlowListAdapter(Context context, List<CashFlow> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cashflow_list_item, null);
            holder = createHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        fillViewWithItem(holder, getItem(position));

        return convertView;
    }

    private ViewHolder createHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.fromWallet = (TextView) convertView.findViewById(R.id.fromWallet);
        holder.toWallet = (TextView) convertView.findViewById(R.id.toWallet);
        holder.amount = (TextView) convertView.findViewById(R.id.amount);
        holder.category = (TextView) convertView.findViewById(R.id.category);
        holder.comment = (TextView) convertView.findViewById(R.id.comment);
        holder.date = (TextView) convertView.findViewById(R.id.date);
        return holder;
    }

    private void fillViewWithItem(ViewHolder holder, CashFlow item) {
        holder.category.setText(getCategoryText(item));
        holder.fromWallet.setText(getFromWalletText(item));
        holder.toWallet.setText(getToWalletText(item));
        holder.amount.setText(NumberFormat.getCurrencyInstance().format(item.getAmount()));
        holder.amount.setTextColor(getAmountColor(item));
        holder.date.setText(getDateText(item));

        if (Strings.isNullOrEmpty(item.getComment())) {
            holder.comment.setVisibility(View.GONE);
        } else {
            holder.comment.setVisibility(View.VISIBLE);
            holder.comment.setText(getLabeledSpannable(getContext().getString(R.string.cashflowListCommentLabel), item.getComment()));
        }
    }

    private String getCategoryText(CashFlow cashFlow) {
        if (cashFlow.getCategory() == null) {
            return "Other";
        }
        if (cashFlow.getCategory().getParent() == null) {
            return cashFlow.getCategory().getName();
        }
        return cashFlow.getCategory().getName() + " (" + cashFlow.getCategory().getParent().getName() + ")";
    }

    private CharSequence getFromWalletText(CashFlow item) {
        if (item.getFromWallet() != null) {
            return getLabeledSpannable(getContext().getString(R.string.cashflowListFromWalletLabel), item.getFromWallet().getName());
        } else {
            return getContext().getString(R.string.cashflowListFromWalletLabel);
        }
    }

    private CharSequence getToWalletText(CashFlow item) {
        if (item.getToWallet() != null) {
            return getLabeledSpannable(getContext().getString(R.string.cashflowListToWalletLabel), item.getToWallet().getName());
        } else {
            return getContext().getString(R.string.cashflowListToWalletLabel);
        }
    }

    private int getAmountColor(CashFlow item) {
        if (item.isExpanse()) {
            return getContext().getResources().getColor(R.color.red);
        } else if (item.isIncome()) {
            return getContext().getResources().getColor(R.color.green);
        } else if (item.isTransfer()) {
            return getContext().getResources().getColor(R.color.blue);
        }
        throw new RuntimeException("Unexpected cashflow type.");
    }

    private String getDateText(CashFlow item) {
        String timeString = DateFormat.getTimeFormat(getContext()).format(item.getDateTime());
        String dateString = DateFormat.getDateFormat(getContext()).format(item.getDateTime());
        return timeString + "\n" + dateString;
    }

    private CharSequence getLabeledSpannable(String label, String text) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(label + " " + text);
        spanTxt.setSpan(new AbsoluteSizeSpan((int) getContext().getResources().getDimension(R.dimen.mediumFontSize)), spanTxt.length() - text.length(), spanTxt.length(), 0);
        return spanTxt;
    }

    private class ViewHolder {
        protected TextView fromWallet;
        protected TextView toWallet;
        protected TextView amount;
        protected TextView category;
        protected TextView comment;
        protected TextView date;
    }
}
