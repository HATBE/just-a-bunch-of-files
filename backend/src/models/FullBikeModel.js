class FullBikeModel {
    #id;
    #user_id;
    #name;
    #make;
    #model;
    #year;
    #fromYear;
    #toYear;

    constructor(user) {
        if(!user) return;
        this.#id = user.id;
        this.#user_id = user.user_id;
        this.#name = user.name;
        this.#model = user.model;
        this.#year = user.year;
        this.#fromYear = user.fromYear;
        this.#toYear = user.toYear;
    }

    getId() {
        return this.#id;
    }

    getUserId() {
        return this.#user_id;
    }

    getName() {
        return this.#name;
    }

    getModel() {
        return this.#model;
    }

    getYear() {
        return this.#year;
    }

    getFromYear() {
        return this.#fromYear;
    }

    getToYear() {
        return this.#toYear;
    }

    getAsObject() {
        return {
            id: this.getId(),
            name: this.getName(),
            user_id: this.getUserId(),
            model: this.getModel(),
            year: this.getYear(),
            toYear: this.getToYear(),
            fromYear: this.getFromYear()
        }
    }
}

module.exports = FullBikeModel;