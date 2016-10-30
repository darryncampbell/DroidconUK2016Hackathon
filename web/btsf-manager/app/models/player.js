export default Ember.Object.extend({

  urlPrefix: 'images/',

  avatarUrl: Ember.computed('name', function () {
    let name = this.get('name');

    if (name === 'Doge') {
      return this.urlPrefix + 'avatar_doge.jpeg'
    }
    if (name === 'Terminator') {
      return this.urlPrefix + 'avatar_terminator.jpg'
    }
    if (name === 'Spiderman') {
      return this.urlPrefix + 'avatar_spiderman.jpg'
    }
    if (name === 'Goku') {
      return this.urlPrefix + 'avatar_goku.jpg'
    }
    if (name === 'Zebra') {
      return this.urlPrefix + 'avatar_zebra.jpg'
    }
  })
});
