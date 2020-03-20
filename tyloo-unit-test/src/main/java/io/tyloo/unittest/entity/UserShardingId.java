package io.tyloo.unittest.entity;


import java.io.Serializable;

/*
 *
 * 用户id
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:31 2019/12/5
 *
 */
public abstract class UserShardingId implements Serializable {

    private static final long serialVersionUID = -8923642703284688507L;
    private Long id;

    private Long userId;

    public UserShardingId() {
    }

    public UserShardingId(Long userId) {
        this.userId = userId;
    }

    public UserShardingId(Long id, Long userId) {

        this.id = id;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        UserShardingId that = (UserShardingId) obj;

        boolean isIdEquals = false;

        boolean isUserIdEquals = false;

        if ((this.id == null && that.id == null) || (this.id != null && this.id.equals(that.id))) {
            isIdEquals = true;
        }

        if ((this.userId == null && that.userId == null) || (this.userId != null && this.userId.equals(that.userId))) {
            isUserIdEquals = true;
        }

        return isIdEquals && isUserIdEquals;
    }

    @Override
    public int hashCode() {

        int hashCode = 17;

        hashCode += this.id == null ? 0 : this.id * 31;

        hashCode += this.userId == null ? 0 : this.userId * 31;

        return hashCode;
    }
}
