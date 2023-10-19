class FullBikeModel {
    #id;
    #user_id;
    #name;
    #make;
    #model;
    #year;
    #fromYear;
    #toYear;

    constructor(bike) {
        if(!bike) return;
        this.#id = bike.id;
        this.#user_id = bike.user_id;
        this.#name = bike.name;
        this.#make = bike.make;
        this.#model = bike.model;
        this.#year = bike.year;
        this.#fromYear = bike.fromYear;
        this.#toYear = bike.toYear;
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

    getMake() {
        return this.#make;
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
            make: this.getMake(),
            model: this.getModel(),
            year: this.getYear(),
            toYear: this.getToYear(),
            fromYear: this.getFromYear()
        }
    }
}

module.exports = FullBikeModel;