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

import android.view.View;

import com.ngandroid.lib.ng.Model;
import com.ngandroid.lib.ng.ModelObserver;

/**
 * Created by tyler on 2/17/15.
 */
class NgFocus extends NgIf {

    private static NgFocus ngFocus = new NgFocus();
    private NgFocus(){}

    static NgFocus getInstance(){return ngFocus;}

    @Override
    protected ModelObserver getModelMethod(final Model model, final View view, final String field) {
        return new ModelObserver() {
            @Override
            public void invoke(String fieldName, Object arg) {
                try {
                    if(model.getValue(field)){
                        view.requestFocus();
                    }else{
                        view.clearFocus();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        };
    }

    @Override
    public int getAttribute() {
        return 0;
    }
}
