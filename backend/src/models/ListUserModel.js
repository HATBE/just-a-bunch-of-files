class ListUserModel {
    #id;
    #bio;
    #username;

    constructor(user) {
        if(!user) return;
        this.#id = user.id;
        this.#bio = user.bio;
        this.#username = user.username;
    }

    getId() {
        return this.#id;
    }

    getUsername() {
        return this.#username;
    }

    getBio() {
        return this.#bio;
    }

    getAsObject() {
        return {
            id: this.getId(),
            bio: this.getBio(),
            username: this.getUsername()
        }
    }
}

module.exports = ListUserModel;