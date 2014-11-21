package pl.dmichalski.agregator.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dmichalski.agregator.entity.Blog;
import pl.dmichalski.agregator.entity.Item;
import pl.dmichalski.agregator.entity.Role;
import pl.dmichalski.agregator.entity.User;
import pl.dmichalski.agregator.exception.RSSException;
import pl.dmichalski.agregator.repository.BlogRepository;
import pl.dmichalski.agregator.repository.RoleRepository;
import pl.dmichalski.agregator.repository.UserRepository;
import pl.dmichalski.agregator.service.BlogService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Daniel
 */
@Service
@Transactional
public class InitDbService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private RssService rssService;

    @Autowired
    private BlogService blogService;

    private User userAdmin;

    @PostConstruct
    public void init() throws RSSException {
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        roleRepository.save(roleUser);

        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");
        roleRepository.save(roleAdmin);

        userAdmin = new User();
        userAdmin.setEnabled(true);
        userAdmin.setName("admin");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        userAdmin.setPassword(encoder.encode("admin"));
        List<Role> roles = new ArrayList<>();
        roles.add(roleAdmin);
        roles.add(roleUser);
        userAdmin.setRoles(roles);
        userRepository.save(userAdmin);

        Blog blog1 = saveBlog("TVN Najnowsze", "http://www.tvn24.pl/najnowsze.xml");
        Blog blog2 = saveBlog("TVN Najważniejsze", "http://www.tvn24.pl/najwazniejsze.xml");
        Blog blog3 = saveBlog("TVN Świat", "http://www.tvn24.pl/wiadomosci-ze-swiata,2.xml");
        Blog blog4 = saveBlog("TVN Sport", "http://sport.tvn24.pl/sport,81,m.xml");

        saveItems(blog1);
        saveItems(blog2);
        saveItems(blog3);
        saveItems(blog4);
    }

    private Blog saveBlog(String name, String url) {
        Blog blog = new Blog();
        blog.setName(name);
        blog.setUrl(url);
        blog.setUser(userAdmin);
        blogRepository.save(blog);
        return blog;
    }

    private void saveItems(Blog blog) throws RSSException {
        List<Item> items = rssService.getItems(blog.getUrl());
        blog.setItems(items);
        blogService.saveItems(blog);
    }

}
