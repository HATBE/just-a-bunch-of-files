class FullUserModel {
    #id;
    #username;
    #email_address;
    #bio;
    #creation_date;

    constructor(user) {
        if(!user) return;
        this.#id = user.id;
        this.#username = user.username;
        this.#email_address = user.email_address;
        this.#bio = user.bio;
        this.#creation_date = user.creation_date;
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

    getCreationDate(/*format = null*/) {
        return this.#creation_date;
    }

    getBio() {
        return this.#bio;
    }

    getAsObject() {
        return {
            id: this.getId(),
            username: this.getUsername(),
            email_address: this.getEmailAddress(),
            bio: this.getBio(),
            creation_date: this.getCreationDate()
        }
    }
}

module.exports = FullUserModel;