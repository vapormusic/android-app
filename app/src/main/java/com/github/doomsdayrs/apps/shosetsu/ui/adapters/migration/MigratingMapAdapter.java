package com.github.doomsdayrs.apps.shosetsu.ui.adapters.migration;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.Doomsdayrs.api.novelreader_core.services.core.objects.Novel;
import com.github.doomsdayrs.apps.shosetsu.R;
import com.github.doomsdayrs.apps.shosetsu.ui.novel.MigrationView;
import com.github.doomsdayrs.apps.shosetsu.ui.viewholders.CompressedHolder;
import com.squareup.picasso.Picasso;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */
public class MigratingMapAdapter extends RecyclerView.Adapter<CompressedHolder> {
    private MigrationView migrationView;

    public MigratingMapAdapter(MigrationView migrationView) {
        this.migrationView = migrationView;
    }

    @NonNull
    @Override
    public CompressedHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.catalogue_item_card, viewGroup, false);
        return new CompressedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompressedHolder holder, int position) {
        Novel novel = migrationView.novelResults.get(migrationView.selection).get(position);
        Picasso.get().load(novel.imageURL).into(holder.image);
        holder.title.setText(novel.title);
        holder.itemView.setOnClickListener(view -> {
            migrationView.novelResults.remove(migrationView.selection);
            migrationView.novels.remove(migrationView.selection);

            if (migrationView.selection + 1 != migrationView.novels.size() - 1) {
                Log.d("Increment","Increase");
                migrationView.selection++;
                refresh();
            } else if (migrationView.selection - 1 != -1) {
                Log.d("Increment","Decrease");
                migrationView.selection--;
                refresh();
            } else migrationView.finish();

        });
    }

    private void refresh() {
        migrationView.selectedNovels.post(migrationView.selectedNovelsAdapters::notifyDataSetChanged);
        migrationView.mappingNovels.post(migrationView.mappingNovelsAdapter::notifyDataSetChanged);
    }
    @Override
    public int getItemCount() {
        return migrationView.novelResults.get(migrationView.selection).size();
    }

}
