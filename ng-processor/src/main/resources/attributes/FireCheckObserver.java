/*
 * Copyright 2015 Tyler Davis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ngandroid.lib.ngattributes;

import android.util.Log;
import android.view.View;

import com.ngandroid.lib.ng.Model;
import com.ngandroid.lib.ng.ModelObserver;

/**
* Created by tyler on 2/10/15.
*/
final class FireCheckObserver implements ModelObserver {

    private final Model model;
    private final String field;
    private final View view;
    private final boolean isGone;

    public FireCheckObserver(Model model, View view, String field, boolean isGone) {
        this.model = model;
        this.field = field;
        this.view = view;
        this.isGone = isGone;
    }

    @Override
    public void invoke(String fieldName, Object arg) {
        try {
            if(model.getValue(field)){
                if(isGone)
                    view.setVisibility(View.GONE);
                else
                    view.setVisibility(View.INVISIBLE);
            }else{
                view.setVisibility(View.VISIBLE);
            }
        } catch (Throwable throwable) {
            Log.e("Visibility", "An error was thrown while getting the value from a model.", throwable);
        }
    }
}
