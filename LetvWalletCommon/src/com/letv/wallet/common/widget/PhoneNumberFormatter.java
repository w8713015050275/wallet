package com.letv.wallet.common.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.widget.TextView;

/**
 * Created by linquan on 15-12-11.
 */
public final class PhoneNumberFormatter {
    private PhoneNumberFormatter() {

    }

    /**
     * Delay-set {@link PhoneNumberFormattingTextWatcher} to a {@link TextView}.
     */
    public static final void setPhoneNumberFormattingTextWatcher(Context context,
                                                                 TextView textView,
                                                                 PhoneNumberFormattingTextWatcherWithAction.ActionCallback callback) {
        new TextWatcherLoadAsyncTask("CN", textView, callback)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    /**
     * Load {@link TextWatcherLoadAsyncTask} in a worker thread and set it to a {@link TextView}.
     */
    private static class TextWatcherLoadAsyncTask extends
            AsyncTask<Void, Void, PhoneNumberFormattingTextWatcherWithAction> {
        private final String mCountryCode;
        private final TextView mTextView;
        private final PhoneNumberFormattingTextWatcherWithAction.ActionCallback mCallback;

        public TextWatcherLoadAsyncTask(String countryCode, TextView textView,
                                        PhoneNumberFormattingTextWatcherWithAction.ActionCallback callback) {
            mCountryCode = countryCode;
            mTextView = textView;
            this.mCallback = callback;
        }

        @Override
        protected PhoneNumberFormattingTextWatcherWithAction doInBackground(Void... params) {
            return new PhoneNumberFormattingTextWatcherWithAction(mCountryCode, mCallback);
        }

        @Override
        protected void onPostExecute(PhoneNumberFormattingTextWatcherWithAction watcher) {
            if (watcher == null || isCancelled()) {
                return; // May happen if we cancel the task.
            }
            // Setting a text changed listener is safe even after the view is detached.
            mTextView.addTextChangedListener(watcher);

            // Note changes the user made before onPostExecute() will not be formatted, but
            // once they type the next letter we format the entire text, so it's not a big deal.
            // (And loading PhoneNumberFormattingTextWatcher is usually fast enough.)
            // We could use watcher.afterTextChanged(mTextView.getEditableText()) to force format
            // the existing content here, but that could cause unwanted results.
            // (e.g. the contact editor thinks the user changed the content, and would save
            // when closed even when the user didn't make other changes.)
        }
    }
}
