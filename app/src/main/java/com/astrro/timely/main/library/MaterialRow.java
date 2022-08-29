package com.astrro.timely.main.library;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.custom.InfiniteScrollAdapter;
import com.astrro.timely.gallery.Image;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MaterialRow extends RecyclerView.ViewHolder implements View.OnClickListener {
   private final int viewType;
   private IDocument document;
   private ImageView img_documentThumbnail;
   private TextView tv_imagePlaceHolder;
   private TextView tv_documentTitle;
   private TextView tv_author;
   private TextView tv_mimeSize;
   private TextView tv_uploadDate;
   private ImageView img_like;
   private boolean isLiked;

   public MaterialRow(@NonNull View itemView, int viewType) {
      super(itemView);
      this.viewType = viewType;

      if (viewType == InfiniteScrollAdapter.VIEW_TYPE_ITEM) {
         img_documentThumbnail = itemView.findViewById(R.id.document_thumbnail);
         tv_imagePlaceHolder = itemView.findViewById(R.id.image_placeholder);
         tv_documentTitle = itemView.findViewById(R.id.document_title);
         tv_author = itemView.findViewById(R.id.author);
         tv_mimeSize = itemView.findViewById(R.id.mime_size);
         tv_uploadDate = itemView.findViewById(R.id.upload_date);
         img_like = itemView.findViewById(R.id.like);
         img_like.setOnClickListener(this);
      }
   }

   @Override
   public void onClick(View v) {
      final int viewId = v.getId();
      if (viewId == R.id.like) {
         this.isLiked = !this.isLiked;
         document.setLiked(this.isLiked);
         img_like.setImageResource(document.isLiked() ? R.drawable.ic_heart_fill : R.drawable.ic_heart);
         sendDocumentLikeRequest();
      }
   }

   // TODO: Add request functions

   private void sendDocumentLikeRequest() {
   }

   public MaterialRow with(List<? extends DataModel> list) {
      this.document = (IDocument) list.get(getAbsoluteAdapterPosition());
      return this;
   }

   public void bindView() {
      if (viewType == InfiniteScrollAdapter.VIEW_TYPE_ITEM) {
         Image documentThumbnail = document.getDocumentThumbnail();
         if (documentThumbnail != null) {
            img_documentThumbnail.setVisibility(View.VISIBLE);
            tv_imagePlaceHolder.setVisibility(View.GONE);
            Picasso.get().load(documentThumbnail.getImageUri()).fit().into(img_documentThumbnail);
         } else {
            img_documentThumbnail.setVisibility(View.GONE);
            tv_imagePlaceHolder.setVisibility(View.VISIBLE);
            String mimetype = document.getMimeType();
            tv_imagePlaceHolder.setText(mimetype);
         }

         tv_documentTitle.setText(document.getDocumentTitle());
         tv_mimeSize.setText(document.getMimeType() + " - " + document.getConvertedDocumentSize());
         tv_uploadDate.setText("UPLOADED - " + document.getUploadDate());
         tv_author.setText(document.getAuthor());

         this.isLiked = document.isLiked();

         img_like.setImageResource(document.isLiked() ? R.drawable.ic_heart_fill : R.drawable.ic_heart);
      }
   }

}
