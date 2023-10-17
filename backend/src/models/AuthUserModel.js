class FullUserModel {
    #id;
    #username;
    #email_address;
    #password_hash;

    constructor(user) {
        if(!user) return;
        this.#id = user.id;
        this.#username = user.username;
        this.#email_address = user.email_address;
        this.#password_hash = user.password_hash;
    }

    getId() {
        return this.#id;
    }

    getUsername() {
        return this.#username;
    }

    getEmailAddress() {
        return this.#email_address;
    }

    getPasswordHash() {
        return this.#password_hash;
    }

    getAsObject() {
        return {
            id: this.getId(),
            username: this.getUsername(),
            email_address: this.getEmailAddress(),
        }
    }
}

module.exports = FullUserModel;