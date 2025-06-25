package com.example.memorix.ui.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.data.remote.dto.Share.IncomingShare;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceivedInviteAdapter extends RecyclerView.Adapter<ReceivedInviteAdapter.ViewHolder> {

    private List<IncomingShare> incomingShares;
    private OnInviteActionListener actionListener;

    public interface OnInviteActionListener {
        void onAcceptInvite(IncomingShare share);
        void onDeclineInvite(IncomingShare share);
    }

    public ReceivedInviteAdapter() {
        this.incomingShares = new ArrayList<>();
        android.util.Log.d("ReceivedInviteAdapter", "Adapter created");
    }

    public void setOnInviteActionListener(OnInviteActionListener listener) {
        this.actionListener = listener;
        android.util.Log.d("ReceivedInviteAdapter", "Action listener set: " + (listener != null));
    }

    public void setIncomingShares(List<IncomingShare> shares) {
        android.util.Log.d("ReceivedInviteAdapter", "setIncomingShares called");
        android.util.Log.d("ReceivedInviteAdapter", "Shares parameter: " + shares);
        android.util.Log.d("ReceivedInviteAdapter", "Shares size: " + (shares != null ? shares.size() : "null"));

        this.incomingShares = shares != null ? shares : new ArrayList<>();
        android.util.Log.d("ReceivedInviteAdapter", "Final shares size: " + this.incomingShares.size());

        notifyDataSetChanged();
    }

    public void addIncomingShare(IncomingShare share) {
        if (share != null) {
            this.incomingShares.add(share);
            notifyItemInserted(incomingShares.size() - 1);
            android.util.Log.d("ReceivedInviteAdapter", "Added share, new size: " + incomingShares.size());
        }
    }

    public void removeIncomingShare(int position) {
        if (position >= 0 && position < incomingShares.size()) {
            incomingShares.remove(position);
            notifyItemRemoved(position);
            android.util.Log.d("ReceivedInviteAdapter", "Removed share at position " + position);
        }
    }

    public void updateIncomingShare(int position, IncomingShare share) {
        if (position >= 0 && position < incomingShares.size() && share != null) {
            incomingShares.set(position, share);
            notifyItemChanged(position);
            android.util.Log.d("ReceivedInviteAdapter", "Updated share at position " + position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("ReceivedInviteAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_received_invite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        android.util.Log.d("ReceivedInviteAdapter", "onBindViewHolder called for position: " + position);

        if (position >= incomingShares.size()) {
            android.util.Log.e("ReceivedInviteAdapter", "Position out of bounds: " + position + " >= " + incomingShares.size());
            return;
        }

        IncomingShare share = incomingShares.get(position);
        android.util.Log.d("ReceivedInviteAdapter", "Binding share: " + share);

        holder.bind(share);
    }

    @Override
    public int getItemCount() {
        int count = incomingShares.size();
        android.util.Log.d("ReceivedInviteAdapter", "getItemCount: " + count);
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSenderAvatar;
        private TextView txtSenderName;
        private TextView txtSenderEmail;
        private TextView txtStatus;
        private TextView txtDeckName;
        private TextView txtDeckDescription;
        private TextView txtPermission;
        private TextView txtTime;
        private Button btnAccept;
        private Button btnDecline;
        private View layoutActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            android.util.Log.d("ViewHolder", "ViewHolder constructor called");

            txtSenderAvatar = itemView.findViewById(R.id.txt_sender_avatar);
            txtSenderName = itemView.findViewById(R.id.txt_sender_name);
            txtSenderEmail = itemView.findViewById(R.id.txt_sender_email);
            txtStatus = itemView.findViewById(R.id.txt_status);
            txtDeckName = itemView.findViewById(R.id.txt_deck_name);
            txtDeckDescription = itemView.findViewById(R.id.txt_deck_description);
            txtPermission = itemView.findViewById(R.id.txt_permission);
            txtTime = itemView.findViewById(R.id.txt_time);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
            layoutActions = itemView.findViewById(R.id.layout_actions);

            // LOG KIỂM TRA VIEWS
            android.util.Log.d("ViewHolder", "txtSenderAvatar: " + (txtSenderAvatar != null));
            android.util.Log.d("ViewHolder", "txtSenderName: " + (txtSenderName != null));
            android.util.Log.d("ViewHolder", "txtDeckName: " + (txtDeckName != null));
            android.util.Log.d("ViewHolder", "btnAccept: " + (btnAccept != null));
            android.util.Log.d("ViewHolder", "layoutActions: " + (layoutActions != null));
        }

        public void bind(IncomingShare share) {
            android.util.Log.d("ViewHolder", "bind() called with share: " + share);

            // HIỂN THỊ THÔNG TIN CÓ SẴN TỪ API RESPONSE
            try {
                // Avatar: Hiển thị ID của người gửi
                String avatarText = String.valueOf(share.getSharedByUserId());
                txtSenderAvatar.setText(avatarText);

                // Tên người gửi: Hiển thị User ID
                String senderName = "User " + share.getSharedByUserId();
                txtSenderName.setText(senderName);

                // Email: Ẩn hoặc hiển thị placeholder
                txtSenderEmail.setText("ID: " + share.getSharedByUserId());

                android.util.Log.d("ViewHolder", "Set sender info from shared_by_user_id: " + share.getSharedByUserId());
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error setting sender info", e);
                txtSenderAvatar.setText("?");
                txtSenderName.setText("Unknown");
                txtSenderEmail.setText("");
            }

            // Set status
            try {
                setStatusText(share.getStatus());
                android.util.Log.d("ViewHolder", "Set status: " + share.getStatus());
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error setting status", e);
                txtStatus.setText("Unknown");
            }

            // HIỂN THỊ THÔNG TIN DECK TỪ API
            try {
                String deckName = share.getDeckName();
                if (deckName != null && !deckName.trim().isEmpty()) {
                    txtDeckName.setText(deckName);
                } else {
                    txtDeckName.setText("Untitled Deck");
                }

                String description = share.getDeckDescription();
                if (description != null && !description.trim().isEmpty()) {
                    txtDeckDescription.setText(description);
                    txtDeckDescription.setVisibility(View.VISIBLE);
                } else {
                    txtDeckDescription.setVisibility(View.GONE);
                }

                android.util.Log.d("ViewHolder", "Set deck info: " + deckName + " / " + description);
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error setting deck info", e);
                txtDeckName.setText("Error loading deck");
                txtDeckDescription.setVisibility(View.GONE);
            }

            // Set permission
            try {
                String permission = getPermissionText(share.getPermissionLevel());
                txtPermission.setText("Quyền: " + permission);
                android.util.Log.d("ViewHolder", "Set permission: " + permission);
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error setting permission", e);
                txtPermission.setText("Quyền: --");
            }

            // Set time - Sử dụng shared_at từ API
            try {
                String timeText = formatTimeAgo(share.getSharedAt());
                txtTime.setText(timeText);
                android.util.Log.d("ViewHolder", "Set time: " + timeText + " (from: " + share.getSharedAt() + ")");
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error setting time", e);
                txtTime.setText("");
            }

            // Handle action buttons dựa trên status
            try {
                if ("pending".equals(share.getStatus())) {
                    layoutActions.setVisibility(View.VISIBLE);

                    btnAccept.setOnClickListener(v -> {
                        android.util.Log.d("ViewHolder", "Accept button clicked for share " + share.getShareId());
                        if (actionListener != null) {
                            actionListener.onAcceptInvite(share);
                        }
                    });

                    btnDecline.setOnClickListener(v -> {
                        android.util.Log.d("ViewHolder", "Decline button clicked for share " + share.getShareId());
                        if (actionListener != null) {
                            actionListener.onDeclineInvite(share);
                        }
                    });

                    android.util.Log.d("ViewHolder", "Action buttons visible for pending status");
                } else {
                    layoutActions.setVisibility(View.GONE);
                    android.util.Log.d("ViewHolder", "Action buttons hidden for status: " + share.getStatus());
                }
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error setting action buttons", e);
                layoutActions.setVisibility(View.GONE);
            }
        }

        private void setStatusText(String status) {
            try {
                switch (status != null ? status.toLowerCase() : "") {
                    case "pending":
                        txtStatus.setText("Chờ duyệt");
                        // txtStatus.setBackgroundResource(R.drawable.bg_status_pending);
                        break;
                    case "accepted":
                        txtStatus.setText("Đã chấp nhận");
                        // txtStatus.setBackgroundResource(R.drawable.bg_status_accepted);
                        break;
                    case "declined":
                        txtStatus.setText("Đã từ chối");
                        // txtStatus.setBackgroundResource(R.drawable.bg_status_declined);
                        break;
                    default:
                        txtStatus.setText(status != null ? status : "Không xác định");
                        // txtStatus.setBackgroundResource(R.drawable.bg_status_pending);
                        break;
                }
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error in setStatusText", e);
                txtStatus.setText("Error");
            }
        }

        private String getPermissionText(String permissionLevel) {
            if (permissionLevel == null) return "Không xác định";

            try {
                switch (permissionLevel.toLowerCase()) {
                    case "view":
                        return "Xem";
                    case "edit":
                        return "Chỉnh sửa";
                    case "admin":
                        return "Quản trị";
                    default:
                        return permissionLevel; // Return original if unknown
                }
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error in getPermissionText", e);
                return "Error";
            }
        }

        private String formatTimeAgo(String sharedAt) {
            if (sharedAt == null || sharedAt.isEmpty()) {
                return "Không rõ thời gian";
            }

            try {
                // Parse ISO date format: "2025-06-24T12:48:41.323Z"
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = isoFormat.parse(sharedAt);

                if (date != null) {
                    long timeDiff = System.currentTimeMillis() - date.getTime();
                    long seconds = timeDiff / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;
                    long days = hours / 24;

                    if (days > 0) {
                        return days + " ngày trước";
                    } else if (hours > 0) {
                        return hours + " giờ trước";
                    } else if (minutes > 0) {
                        return minutes + " phút trước";
                    } else {
                        return "Vừa xong";
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("ReceivedInviteAdapter", "Error parsing date: " + sharedAt, e);
            }

            return "Không rõ thời gian";
        }
    }
}