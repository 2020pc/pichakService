package com.caspian.pichak.model.dto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.NonNull;

public class ISOMessageDTO {
    private @NonNull long rrn;
    private @NonNull String pan;
    private @NonNull User user;
    private @NonNull int totalLength;
    private @NonNull int serviceCode;
    private @NonNull String date;
    private @NonNull String time;
    private @NonNull String locale;
    private String sessionId;
    private @NonNull JsonObject body;

    public String toString() {
        return (new Gson()).toJson(this);
    }

    public @NonNull long getRrn() {
        return this.rrn;
    }

    public @NonNull String getPan() {
        return this.pan;
    }

    public @NonNull User getUser() {
        return this.user;
    }

    public @NonNull int getTotalLength() {
        return this.totalLength;
    }

    public @NonNull int getServiceCode() {
        return this.serviceCode;
    }

    public @NonNull String getDate() {
        return this.date;
    }

    public @NonNull String getTime() {
        return this.time;
    }

    public @NonNull String getLocale() {
        return this.locale;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public @NonNull JsonObject getBody() {
        return this.body;
    }

    public void setRrn(final @NonNull long rrn) {
        this.rrn = rrn;
    }

    public void setPan(final @NonNull String pan) {
        if (pan == null) {
            throw new NullPointerException("pan is marked non-null but is null");
        } else {
            this.pan = pan;
        }
    }

    public void setUser(final @NonNull User user) {
        if (user == null) {
            throw new NullPointerException("user is marked non-null but is null");
        } else {
            this.user = user;
        }
    }

    public void setTotalLength(final @NonNull int totalLength) {
        this.totalLength = totalLength;
    }

    public void setServiceCode(final @NonNull int serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setDate(final @NonNull String date) {
        if (date == null) {
            throw new NullPointerException("date is marked non-null but is null");
        } else {
            this.date = date;
        }
    }

    public void setTime(final @NonNull String time) {
        if (time == null) {
            throw new NullPointerException("time is marked non-null but is null");
        } else {
            this.time = time;
        }
    }

    public void setLocale(final @NonNull String locale) {
        if (locale == null) {
            throw new NullPointerException("locale is marked non-null but is null");
        } else {
            this.locale = locale;
        }
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public void setBody(final @NonNull JsonObject body) {
        if (body == null) {
            throw new NullPointerException("body is marked non-null but is null");
        } else {
            this.body = body;
        }
    }

    public static class User {
        private @NonNull String customerId;
        private @NonNull String nationalCode;
        private @NonNull String shahabCode;
        private @NonNull String accessToken;
        private @NonNull String mobile;
        private @NonNull String firstName;
        private @NonNull String lastName;
        private @NonNull String clientType;

//        public User(ISOMessageDTO msg) {
//            this.accessToken = msg.getUser().accessToken;
//            this.clientType = msg.getUser().clientType;
//            this.nationalCode = msg.getUser().nationalCode;
//            this.shahabCode = msg.getUser().shahabCode;
//            this.mobile = msg.getUser().mobile;
//            this.firstName = msg.getUser().firstName;
//            this.lastName = msg.getUser().lastName;
//            this.customerId = msg.getUser().customerId;
//        }

        public String toString() {
            return (new Gson()).toJson(this);
        }

        public @NonNull String getCustomerId() {
            return this.customerId;
        }

        public @NonNull String getNationalCode() {
            return this.nationalCode;
        }

        public @NonNull String getShahabCode() {
            return this.shahabCode;
        }

        public @NonNull String getAccessToken() {
            return this.accessToken;
        }

        public @NonNull String getMobile() {
            return this.mobile;
        }

        public @NonNull String getFirstName() {
            return this.firstName;
        }

        public @NonNull String getLastName() {
            return this.lastName;
        }

        public @NonNull String getClientType() {
            return this.clientType;
        }

        public void setCustomerId(final @NonNull String customerId) {
            if (customerId == null) {
                throw new NullPointerException("customerId is marked non-null but is null");
            } else {
                this.customerId = customerId;
            }
        }

        public void setNationalCode(final @NonNull String nationalCode) {
            if (nationalCode == null) {
                throw new NullPointerException("nationalCode is marked non-null but is null");
            } else {
                this.nationalCode = nationalCode;
            }
        }

        public void setShahabCode(final @NonNull String shahabCode) {
            if (shahabCode == null) {
                throw new NullPointerException("shahabCode is marked non-null but is null");
            } else {
                this.shahabCode = shahabCode;
            }
        }

        public void setAccessToken(final @NonNull String accessToken) {
            if (accessToken == null) {
                throw new NullPointerException("accessToken is marked non-null but is null");
            } else {
                this.accessToken = accessToken;
            }
        }

        public void setMobile(final @NonNull String mobile) {
            if (mobile == null) {
                throw new NullPointerException("mobile is marked non-null but is null");
            } else {
                this.mobile = mobile;
            }
        }

        public void setFirstName(final @NonNull String firstName) {
            if (firstName == null) {
                throw new NullPointerException("firstName is marked non-null but is null");
            } else {
                this.firstName = firstName;
            }
        }

        public void setLastName(final @NonNull String lastName) {
            if (lastName == null) {
                throw new NullPointerException("lastName is marked non-null but is null");
            } else {
                this.lastName = lastName;
            }
        }

        public void setClientType(final @NonNull String clientType) {
            if (clientType == null) {
                throw new NullPointerException("clientType is marked non-null but is null");
            } else {
                this.clientType = clientType;
            }
        }

        public User(final @NonNull String customerId, final @NonNull String nationalCode, final @NonNull String shahabCode, final @NonNull String accessToken, final @NonNull String mobile, final @NonNull String firstName, final @NonNull String lastName, final String clientType) {
            if (customerId == null) {
                throw new NullPointerException("customerId is marked non-null but is null");
            } else if (nationalCode == null) {
                throw new NullPointerException("nationalCode is marked non-null but is null");
            } else if (shahabCode == null) {
                throw new NullPointerException("shahabCode is marked non-null but is null");
            } else if (accessToken == null) {
                throw new NullPointerException("accessToken is marked non-null but is null");
            } else if (mobile == null) {
                throw new NullPointerException("mobile is marked non-null but is null");
            } else if (firstName == null) {
                throw new NullPointerException("firstName is marked non-null but is null");
            } else if (lastName == null) {
                throw new NullPointerException("lastName is marked non-null but is null");
            } else if (clientType == null) {
                throw new NullPointerException("clientType is marked non-null but is null");
            } else {
                this.customerId = customerId;
                this.nationalCode = nationalCode;
                this.shahabCode = shahabCode;
                this.accessToken = accessToken;
                this.mobile = mobile;
                this.firstName = firstName;
                this.lastName = lastName;
                this.clientType = clientType;
            }
        }

        public User() {
        }
    }
}
