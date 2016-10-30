import Player from '../models/player'

export default Ember.Object.extend({

  init: function() {
    this.set('source', Player.create(this.get('source')));
    this.set('target', Player.create(this.get('target')));
  }

});
