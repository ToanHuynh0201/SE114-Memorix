package com.example.memorix.view.share;

import android.annotation.SuppressLint;
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
import java.util.TimeZone;

public class ReceivedInviteAdapter extends RecyclerView.Adapter<ReceivedInviteAdapter.ViewHolder> {

    private List<IncomingShare> incomingShares;
    private OnInviteActionListener actionListener;

    public interface OnInviteActionListener {
        void onAcceptInvite(IncomingShare share);
        void onDeclineInvite(IncomingShare share);
    }

    public ReceivedInviteAdapter() {
        this.incomingShares = new ArrayList<>();
    }

    public void setOnInviteActionListener(OnInviteActionListener listener) {
        this.actionListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setIncomingShares(List<IncomingShare> shares) {
        this.incomingShares = shares != null ? shares : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addIncomingShare(IncomingShare share) {
        if (share != null) {
            this.incomingShares.add(share);
            notifyItemInserted(incomingShares.size() - 1);
        }
    }

    public void removeIncomingShare(int position) {
        if (position >= 0 && position < incomingShares.size()) {
            incomingShares.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateIncomingShare(int position, IncomingShare share) {
        if (position >= 0 && position < incomingShares.size() && share != null) {
            incomingShares.set(position, share);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_received_invite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncomingShare share = incomingShares.get(position);
        holder.bind(share);
    }

    @Override
    public int getItemCount() {
        int count = incomingShares.size();
        android.util.Log.d("ReceivedInviteAdapter", "getItemCount: " + count);
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtSenderAvatar;
        private final TextView txtSenderName;
        private final TextView txtStatus;
        private final TextView txtDeckName;
        private final TextView txtDeckDescription;
        private final TextView txtPermission;
        private final TextView txtTime;
        private final Button btnAccept;
        private final Button btnDecline;
        private final View layoutActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            android.util.Log.d("ViewHolder", "ViewHolder constructor called");

            txtSenderAvatar = itemView.findViewById(R.id.txt_sender_avatar);
            txtSenderName = itemView.findViewById(R.id.txt_sender_name);
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

        @SuppressLint("SetTextI18n")
        public void bind(IncomingShare share) {
            android.util.Log.d("ViewHolder", "bind() called with share: " + share);

            // HIỂN THỊ THÔNG TIN NGƯỜI GỬI (bỏ user ID)
            try {
                // Avatar: Hiển thị ký tự đầu của "Anonymous"
                txtSenderAvatar.setText("A");

                // Tên người gửi: Hiển thị "Anonymous User"
                txtSenderName.setText("Anonymous User");

                android.util.Log.d("ViewHolder", "Set sender info as anonymous");
            } catch (Exception e) {
                android.util.Log.e("ViewHolder", "Error setting sender info", e);
                txtSenderAvatar.setText("?");
                txtSenderName.setText("Unknown");
            }

            // Set status
            try {
                setStatusText(share.getStatus());
            } catch (Exception e) {
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

            // Set time - Chuyển đổi từ UTC sang UTC+7
            try {
                String timeText = formatTimeAgoUTC7(share.getSharedAt());
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
                            // Disable buttons to prevent multiple clicks
                            btnAccept.setEnabled(false);
                            btnDecline.setEnabled(false);

                            actionListener.onAcceptInvite(share);

                            // Re-enable buttons after a short delay
                            btnAccept.postDelayed(() -> {
                                btnAccept.setEnabled(true);
                                btnDecline.setEnabled(true);
                            }, 2000);
                        }
                    });

                    btnDecline.setOnClickListener(v -> {
                        android.util.Log.d("ViewHolder", "Decline button clicked for share " + share.getShareId());
                        if (actionListener != null) {
                            // Disable buttons to prevent multiple clicks
                            btnAccept.setEnabled(false);
                            btnDecline.setEnabled(false);

                            actionListener.onDeclineInvite(share);

                            // Re-enable buttons after a short delay
                            btnAccept.postDelayed(() -> {
                                btnAccept.setEnabled(true);
                                btnDecline.setEnabled(true);
                            }, 2000);
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

        @SuppressLint("SetTextI18n")
        private void setStatusText(String status) {
            try {
                switch (status != null ? status.toLowerCase() : "") {
                    case "pending":
                        txtStatus.setText("Chờ duyệt");
                        txtStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                        break;
                    case "accepted":
                        txtStatus.setText("Đã chấp nhận");
                        txtStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                        break;
                    case "declined":
                        txtStatus.setText("Đã từ chối");
                        txtStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                        break;
                    default:
                        txtStatus.setText(status != null ? status : "Không xác định");
                        txtStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                        break;
                }
            } catch (Exception e) {
                txtStatus.setText("Error");
                txtStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
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

        /**
         * Format time ago với múi giờ UTC+7 (Vietnam timezone)
         * @param sharedAt UTC time string từ server
         * @return Formatted time string
         */
        private String formatTimeAgoUTC7(String sharedAt) {
            if (sharedAt == null || sharedAt.isEmpty()) {
                return "Không rõ thời gian";
            }

            try {
                // Parse ISO date format: "2025-06-24T12:48:41.323Z"
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                // Đặt timezone của parser thành UTC để parse đúng thời gian UTC
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date utcDate = isoFormat.parse(sharedAt);

                if (utcDate != null) {
                    // Lấy thời gian hiện tại theo múi giờ UTC+7
                    long currentTimeUTC7 = System.currentTimeMillis();

                    // Tính thời gian chênh lệch
                    long timeDiff = currentTimeUTC7 - utcDate.getTime();
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

                // Fallback: thử parse với format khác nếu có
                try {
                    // Thử format không có milliseconds
                    SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    altFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date altDate = altFormat.parse(sharedAt);

                    if (altDate != null) {
                        long currentTimeUTC7 = System.currentTimeMillis();
                        long timeDiff = currentTimeUTC7 - altDate.getTime();
                        long hours = (timeDiff / 1000) / 3600;

                        if (hours > 24) {
                            return (hours / 24) + " ngày trước";
                        } else if (hours > 0) {
                            return hours + " giờ trước";
                        } else {
                            return "Vừa xong";
                        }
                    }
                } catch (Exception fallbackException) {
                    android.util.Log.e("ReceivedInviteAdapter", "Fallback parsing also failed", fallbackException);
                }
            }

            return "Không rõ thời gian";
        }
    }
}