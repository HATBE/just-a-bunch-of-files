class Pagination {
    #page;
    #maxCount;
    #maxPages;
    #hasNext;
    #hasLast;
    #limit;
    #start;

    constructor(page, maxCount, limit = 7) {
        this.#limit = limit;
        this.#page = +page;
        this.#maxCount = maxCount;
        this.#maxPages = Math.ceil(this.#maxCount / this.#limit);

        if(this.#page > this.#maxPages) {
            this.#page = this.#maxPages;
        }

        if(this.#page <= 0) {
            this.#page = 1;
        }

        this.#start = (this.#page * this.#limit) - this.#limit;
        this.#start = this.#start <= 0 ? 0 : this.#start;

        this.#hasLast = this.#page > 1 ? true : false;
        this.#hasNext = this.#page <= this.#maxPages -1 ? true : false; 
    }

    getPage() {
        return this.#page;
    }

    getMaxPages() {
        return this.#maxPages;
    }

    getMaxCount() {
        return this.#maxCount;
    }

    hasNext() {
        return this.#hasNext;
    }

    hasLast() {
        return this.#hasLast;
    }

    getLimit() {
        return this.#limit;
    }

    getStart() {
        return this.#start;
    }

    getAsObject() {
        return {
            page: this.getPage(),
            maxPages: this.getMaxPages(),
            maxCount: this.getMaxCount(),
            hasLast: this.hasLast(),
            hasNext: this.hasNext(),
            limit: this.getLimit()
        }
    }
}

module.exports = Pagination;